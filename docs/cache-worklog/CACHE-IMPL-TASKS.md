# 검색 캐시 구현 태스크 플랜 (stacked on worktree-perf-baseline-p1)

> 스펙(상세)= `docs/search-cache-design.md`(승인본). 이 파일은 그걸 태스크로 쪼갠 것.
> 워크트리: `.claude/worktrees/search-cache-p1`, 브랜치 `worktree-search-cache-p1`(base `worktree-perf-baseline-p1`).
> Base 위에 이미 ES 검색이 구현돼 있음: `SearchEngineRouter.route(engine, domain, keywordLength, Supplier es, Supplier db)`(global/search), `ProgramQueryServiceImpl.searchPrograms(memberId, req)` / `FacilityQueryServiceImpl.searchFacilityListByKeyword(memberId, keyword)`가 라우터 호출, `SearchMetrics`(global/search), `SearchProperties`(movemap.search: {program,facility}.engine, reconcile.*), `BulkReindexer`/`IndexBootstrapper`(global/search/index), 응답 DTO `ProgramSimpleListResponse`/`FacilitySimpleListResponse`. Redis 스택 존재(spring-boot-starter-data-redis; movemap-perf-redis @ localhost:6379 / redis:6379; StringRedisTemplate auto-config).

## GLOBAL CONSTRAINTS
1. **캐시는 검색 계약을 안 바꾼다** — 컨트롤러/DTO/QueryService 인터페이스 불변. 캐시는 서비스 impl에서 라우터 호출을 감싸는 얇은 계층.
2. **읽기=cache-aside, 무효화=버전 기반**(설계 §4.0/§4.4). 쓰기 전략 없음(런타임 write 경로 없음).
3. **캐시는 선택 계층 — 실패해도 검색은 된다**: Redis/로컬 예외 시 로그+메트릭 후 loader로 우회(§4.6). 빈 catch 금지.
4. **기본 off**: `movemap.search.cache.enabled=false` 기본. 플래그로 켠다(롤아웃 안전). perf 프로파일에선 켤 수 있게.
5. **키 = 검증된 정규화 키워드**: program은 `normalizedKeyword()`(공백 제거), 공통 소문자화+trim. 키 형식은 설계 §4.2. 버전 `v{ver}` + `{engine}` 포함.
6. **negative 캐싱**(빈 결과 짧은 TTL) + **TTL 지터**(설계 §4.3/4.5).
7. **관측**: `SearchMetrics` 확장 — cache hit/miss/error 카운터. 조용한 실패 금지.
8. 패키지: `global/search/cache/`. 설정 빈은 `global/config/`(기존 *Properties 등록 관례 따름).
9. 검증 우선(컴파일/부팅/실측 출력으로 증명). 커밋 메시지 끝 `Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>`. push 금지(PR 태스크에서만).
10. 인프라: 로컬 Redis=`movemap-perf-redis`(localhost:6379). ES/DB 스택 이미 실행 중(program_v1/facility_v1 색인됨, app 컨테이너는 8080). 로컬 bootRun은 8099 포트 사용.

## TASKS

### CT1 — Stage 1: Redis cache-aside (전체)
- `global/config/SearchCacheProperties` @ConfigurationProperties("movemap.search.cache"): `enabled`(false), `ttl`(Duration 10m), `jitter`(Duration 60s), `negativeTtl`(30s), + (Stage2용 자리) `local.enabled`(false)/`local.maxSize`(1000)/`local.ttl`(60s). 기존 관례대로 등록.
- `global/search/cache/SearchCacheVersion`: Redis `mm:search:version`(정수) 읽기/증가. `current()`(짧게 로컬 캐시해 매요청 Redis read 회피, 예: 5초), `bump()`(INCR). Redis 실패 시 안전한 기본(예: 버전 0 취급)으로 우회.
- `global/search/cache/SearchCacheKey`: program `mm:search:v{ver}:program:{engine}:{normKw}:{cursor}:{size}`, facility `mm:search:v{ver}:facility:{engine}:{normKw}`. normKw=소문자+trim(program은 공백제거까지).
- `global/search/cache/SearchResultCache`: `<T> T getOrLoad(String key, Class<T> type, Supplier<T> loader)` — Redis 조회(히트=역직렬화 반환) → 미스=loader 실행 → 저장(빈 결과=negativeTtl, 아니면 ttl+지터). 모든 Redis 접근 try/catch로 감싸 실패 시 loader 우회 + `SearchMetrics` cacheError. hit/miss 계측. StringRedisTemplate + ObjectMapper(레코드 직렬화 라운드트립 확인).
- **무효화 훅**: 재색인 완료 지점(`BulkReindexer` 전량 재색인 후 / 관리 `/internal/search/reindex` 후 / alias 스왑 후)에 `SearchCacheVersion.bump()` 호출. (색인이 바뀌면 버전 올라가 옛 캐시 자동 무효)
- 서비스 impl 배선: `ProgramQueryServiceImpl`/`FacilityQueryServiceImpl`에서 키 만들고 `searchResultCache.getOrLoad(key, RespType.class, () -> router.route(...))`. `enabled=false`면 그냥 loader(캐시 우회).
- `SearchMetrics` 확장: `cacheHit()/cacheMiss()/cacheError()`.
- `application-perf.yml`: `movemap.search.cache` 기본값 추가.
- **검증**: `./gradlew compileJava`. 로컬 부팅(8099, engine=es, es.enabled=true, cache.enabled=true, Redis localhost:6379) → 같은 키워드 2회 조회 → 1회 미스/2회 히트(메트릭 or 로그 or `redis-cli KEYS 'mm:search:*'`로 키 확인). 재색인(관리 엔드포인트 or 재기동) 후 버전 증가로 다음 조회 미스 확인. Redis 죽였을 때(잘못된 포트) 검색이 그대로 동작(우회) 확인. 종료 후 8099 free.
- 모델: opus(다파일 통합·정합성).

### CT2 — Stage 2: 로컬(Caffeine) + Redis 2-tier
- `build.gradle`: `com.github.ben-manes.caffeine:caffeine` 추가(Boot BOM 버전).
- `global/search/cache/LocalSearchCache`(또는 Config 빈): Caffeine `maximumSize`=props.local.maxSize, `expireAfterWrite`=props.local.ttl, recordStats. 키에 `v{ver}` 포함되어 버전 오르면 자동 무효.
- `SearchResultCache`를 로컬→Redis→loader 순으로 확장(설계 §5.2). `local.enabled=false`면 로컬 건너뜀(Stage1과 동일 동작).
- 메트릭: 로컬 히트 카운터(선택). 
- **검증**: compile + 부팅(local.enabled=true) → 로컬 히트 시 Redis 미조회(로컬 캐시 stats 또는 redis MONITOR로 확인), 버전 증가 시 로컬도 무효. Stage1 회귀 없음.
- 모델: sonnet.

### CT3 — 테스트 (Testcontainers Redis + ES)
- Testcontainers Redis(+기존 ES 인프라 재사용)로: ①hit/miss(2회 조회, 2회차 loader 미호출) ②버전 무효화(bump 후 미스) ③negative(빈 결과 캐시·짧은 TTL) ④failover(Redis 다운→예외 없이 loader, cacheError 증가) ⑤2-tier(로컬 히트 시 Redis 미조회; 버전 오르면 로컬 무효) ⑥직렬화 라운드트립(DTO 필드 보존). 동등성 회귀 아님.
- **검증**: `./gradlew test --tests '*Cache*'` green(colima면 DOCKER_HOST + RYUK_DISABLED env 필요 — base 워크트리 T6 노트 참조).
- 모델: sonnet.

### CT4 — Stacked draft PR
- push `worktree-search-cache-p1`, `gh pr create --draft --base worktree-perf-baseline-p1`(stacked). 본문에 캐시 설계 요약·전략 근거(§4.0)·검증·follow-up. assignee @me. draft 확인.
