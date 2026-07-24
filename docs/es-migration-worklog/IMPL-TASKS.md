# ES 자동완성 마이그레이션 — 구현 태스크 플랜 (실행용)

> 이 파일은 subagent-driven-development의 실행 대상 플랜이다. 각 태스크는 implementer→reviewer 루프로 처리한다.
> 상위 설계 근거: `docs/es-migration-implementation-plan.md`. 코드 사실: `docs/es-migration-worklog/01-codebase-facts.md`.
> 리포 루트(worktree): `/Users/onam-ui/Desktop/취업/Portfolio/MoveMap-Back/.claude/worktrees/perf-baseline-p1`
> Base package: `org.wemightmove.movemap` · Java 17 · Spring Boot 3.5.7 · Gradle 8.14.3

---

## GLOBAL CONSTRAINTS (모든 태스크에 구속력)

1. **범위 = 자동완성 `/search` 2개만**: `GET /facilities/search`, `GET /programs/search`. 뷰포트(`/markers`,`/list`)는 절대 건드리지 않는다.
2. **API 계약 불변**: 컨트롤러 시그니처와 응답 DTO(`FacilitySimpleListResponse{List<FacilitySimpleInfo>{id,name,facilityType,facilitySubtype,address}}`, `ProgramSimpleListResponse{List<ProgramSimpleItem>{id,programName,facilityName,facilitySubtype,address}, nextCursor, hasNext, currentSize}`)는 **바이트 동일**하게 유지. ES 문서 타입이 컨트롤러/DTO로 새어나가면 안 됨(어댑터에서 기존 DTO로 매핑).
3. **엔진 스위칭**: 도메인별 feature flag `movemap.search.{facility,program}.engine = es|db`(기본 `db`). flag=db거나 ES 호출 실패 시 **기존 native 쿼리로 fallback**, fallback은 **로그(WARN, 원문 아닌 keyword 길이) + Micrometer 카운터**로 반드시 관측(조용한 다운그레이드 금지).
4. **completion suggester 사용 금지.** 자동완성은 **edge_ngram(min_gram:2) + `match`(nori) + `search_after` + id tie-breaker** 단일 메커니즘. 접두/부분매칭은 edge_ngram이 담당.
5. **Outbox 미도입.** outbox 테이블/relay/seam 코드 만들지 않는다. 동기화 = 초기 전량 색인 + content-hash 리컨실리에이션 + (선택)재색인 훅.
6. **정합성**: PG=원본(SoT), ES=파생. ES `_id`=PG PK. bulk 색인 시 **`BulkResponse.errors()` 항목별 검사 필수**(200이어도 개별 실패 가능) → 로그+메트릭. 빈 catch 금지.
7. **매핑 규칙**: `dynamic: strict`. Nori `decompound_mode: mixed`. `nori_part_of_speech` stoptags에서 **XPN 제거**. 텍스트 필드만(geo/price/bitmask 없음). 내부 필드 `content_hash`(keyword), `source_updated_at`(date)는 클라이언트로 직렬화하지 않음.
8. **클라이언트 = 하이브리드**: 의존성은 `spring-boot-starter-data-elasticsearch`(Boot 3.5.7 BOM), 그러나 bulk·타입드 쿼리·alias는 저수준 `co.elastic.clients.elasticsearch.ElasticsearchClient` 직접 사용.
9. **인젝션 안전**: 사용자 keyword는 항상 `match`/`match_phrase_prefix` 등 타입드 빌더의 바운드 값으로만. `query_string`/`wildcard`/`regexp`/script 금지.
10. **패키지 관례**: 도메인 우선. 도메인별 검색 코드는 `domain/{facility,program}/search/`, 교차 인프라(색인/리컨실/클라이언트/메트릭)는 `global/search/`, 설정 빈은 `global/config/`.
11. **로컬 self-host ES**: perf용 ES는 xpack.security **off**(로컬 격리망, 벤치마크 단순화). `ElasticsearchConfig`는 auth/TLS를 옵션으로 지원하되 기본은 인증 없음. 프로덕션은 보안 ON임을 주석/문서로 명시.
12. **테스트 안전망**: 동등성(옛 결과와 동일) 회귀는 만들지 않는다(형태소로 결과 달라지는 게 정상). intent/contract/fallback/injection/reconciliation 위주.
13. **커밋**: 각 태스크 끝에 커밋. 커밋 메시지 끝에 `Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>`. 브랜치 = `worktree-perf-baseline-p1`(현재), main 아님 — OK.
14. **검증 우선**: "컴파일/테스트 통과"를 실제 명령 출력으로 증명. 추측으로 완료 보고 금지.

---

## 인프라 사실 (구현에 필요)

- perf 스택이 이미 실행 중: `movemap-perf-app`(image `perf-app`), `movemap-perf-db`(postgis/postgis:16-3.4, seed 로드됨: facility ~5.5만/program ~22.8만), `movemap-perf-redis`. 네트워크 `movemap-perf-net`. DB 리소스 2CPU/2GB, app 2CPU/2GB 제한.
- perf harness: `perf/docker-compose.yml`, `perf/Dockerfile`(app 이미지), `perf/k6/{search_perf.js,run.sh,keywords.json}`, `perf/out/`(결과), `perf/AS-IS-BASELINE-REPORT.md`(리포트 포맷 참조).
- k6 실행: `perf/k6/run.sh` (docker `grafana/k6`, `movemap-perf-net` 조인, `MODE/RATE/DURATION/ENDPOINT/COND_TAG` env). AS-IS는 open-model constant-arrival-rate.
- 측정 계정: `perf@test.com` / `Test1234!` (이미 존재).
- Flyway 최신 버전 = V17 → 새 마이그레이션은 V18.

---

## TASKS

### T1 — ES 의존성·설정·로컬 ES 컨테이너 (foundation, 최우선)
**목표**: 앱이 ES에 붙을 수 있는 최소 토대 + 로컬 ES(Nori) 기동.
- `build.gradle`: `implementation 'org.springframework.boot:spring-boot-starter-data-elasticsearch'` 추가(Boot BOM이 버전 관리). 기존 의존성 건드리지 말 것.
- `global/config/ElasticsearchConfig`(extends `ElasticsearchConfiguration`) + `@ConfigurationProperties("movemap.es")` `EsProperties`(host, username?, password?, apiKey?, tls?, connectTimeoutMs 기본 2000, socketTimeoutMs 기본 5000). auth/TLS는 값 있을 때만 적용. `ElasticsearchClient`(저수준) 빈도 노출(ElasticsearchConfiguration이 자동 제공).
- `SearchProperties` `@ConfigurationProperties("movemap.search")`: `facility.engine`, `program.engine`(기본 db), `reconcile.cron`(기본 `0 */15 * * * *`), `reconcile.enabled`(기본 true).
- `application-perf.yml`에 `movemap.es.host=${ES_HOST:localhost:9200}`, `movemap.search.*` 기본값 추가(엔진 기본 db).
- `perf/es/Dockerfile`(elasticsearch:8.x + `bin/elasticsearch-plugin install --batch analysis-nori`), `perf/docker-compose.yml`에 `elasticsearch` 서비스 추가(single-node, xpack.security.enabled=false, ES_JAVA_OPTS 힙 1g, 리소스 제한 2CPU/2GB, `movemap-perf-net`, 9200 노출, healthcheck). app 서비스 `environment`에 `ES_HOST: elasticsearch:9200` 추가하고 `depends_on: elasticsearch`.
- **검증**: `./gradlew compileJava` 성공. `docker compose -f perf/docker-compose.yml up -d elasticsearch` 후 `curl localhost:9200/_cat/plugins`에 `analysis-nori` 보임 + `curl localhost:9200`이 200.
- 모델: standard(sonnet).

### T2 — 감사 컬럼 마이그레이션 V18
**목표**: 확장성·리컨실 델타용 created_at/updated_at.
- `src/main/resources/db/migration/V18__add_audit_columns_for_search.sql`: `facility`·`program`에 `created_at timestamptz NOT NULL DEFAULT now()`, `updated_at timestamptz NOT NULL DEFAULT now()` 추가 + `touch_updated_at()` 트리거 함수 + 두 테이블 BEFORE UPDATE 트리거 + `updated_at` 인덱스.
- **검증**: `docker exec movemap-perf-db psql -U movemap -d movemap -c "\d facility"`에 컬럼/트리거 확인(마이그레이션은 앱 재기동 시 적용되므로, SQL 문법은 psql로 dry 검증하거나 Flyway validate). 최소 SQL을 psql로 직접 적용해보고 롤백 없이 성공하는지 확인.
- 모델: cheap(sonnet).

### T3 — ES 문서·매핑·인덱스 부트스트랩
**목표**: `program_v1`/`facility_v1` 인덱스(매핑) + alias 생성.
- `global/search/index/`에 매핑/세팅 JSON 리소스(`program-mapping.json`, `facility-mapping.json`) — GLOBAL CONSTRAINTS §7 규칙. program: `name`(text nori + keyword + `ngram`(edge_ngram min_gram:2)), `facility_name`(text nori + keyword), `facility_subtype`(keyword), `address`(text nori), `content_hash`(keyword), `source_updated_at`(date). facility: `name`, `facility_type`(keyword), `facility_subtype`(keyword), `address`(text nori), `content_hash`, `source_updated_at`(+`ngram` on name).
- 분석기 세팅: tokenizer `nori_user`(mixed), filter `nori_pos`(XPN 제거), `edge_ngram_filter`(min_gram:2,max_gram:20), analyzers `nori_index`/`nori_search`(동일: nori_pos+lowercase), `ac_index`(nori_user+lowercase+edge_ngram)/`ac_search`(nori_user+lowercase).
- `IndexBootstrapper`(ApplicationRunner 또는 @PostConstruct): 인덱스 `*_v1` 없으면 매핑으로 생성 + alias `program_search`/`facility_search` 부여(멱등). 저수준 `ElasticsearchClient` 사용.
- **검증**: 앱을 로컬(gradle bootRun, perf 프로파일, ES_HOST=localhost:9200)로 띄우거나 통합테스트로 부트스트랩 실행 → `curl localhost:9200/_cat/indices`에 `program_v1`,`facility_v1`, `curl localhost:9200/_cat/aliases`에 alias 확인. (앱 전체 기동이 무거우면 Testcontainers 없이 저수준 클라이언트 단위 실행으로 검증해도 됨.)
- 모델: standard(sonnet).

### T4 — 색인 파이프라인(초기 전량 색인 + 리컨실리에이션)
**목표**: PG→ES 색인 + 드리프트 자가 치유.
- `global/search/index/BulkReindexer`: program·facility를 keyset 스트림(native SQL, id 오름차순 5000씩)으로 읽어 `*Doc`(content_hash=핵심 텍스트필드 sha256, source_updated_at=updated_at) 구성 → 저수준 `ElasticsearchClient` bulk(_id=PK, op index). **`BulkResponse.errors()` 검사** → 실패 항목 로그+`bulkItemFailures` 메트릭. 색인 대상 필드는 §계약의 검색/표시 필드만.
- `global/search/reconcile/ReconciliationJob` `@Scheduled(cron=${movemap.search.reconcile.cron})`: `updated_at > lastRun` 델타 → content_hash 비교 → 다른 것만 재색인 + `count(PG)` vs `count(alias)` → `reconciliation_drift` 게이지, `reconciliation_last_success` 타임스탬프. lastRun은 인메모리(단순) 또는 ES에 watermark 문서.
- 최초 색인 트리거: 앱 기동 시 인덱스가 비어있으면 전량 색인(부트스트랩과 연계) + 수동 `POST /internal/search/reindex`(auth-gated, ADMIN 없으면 인증된 사용자로 임시 — 단 whitelist 추가 금지). 관리 엔드포인트는 간단히.
- **검증**: 로컬 ES + perf DB(localhost:5432) 대상으로 reindex 실행 → `curl 'localhost:9200/program_search/_count'` ≈ program 행수, facility도. 통합테스트(Testcontainers)로 bulk 오류 검사·리컨실 치유를 커버(T6와 조율).
- 모델: **opus**(정합성/에러처리 판단).

### T5 — 검색 어댑터·포트·flag·fallback·메트릭
**목표**: `/search`가 ES로 서빙되되 flag·fallback 유지.
- `domain/program/search/`: `ProgramSearchPort` + `EsProgramSearchAdapter`(bool should [match(name), match(name.ngram), match(facility_name)] + sort _score desc, id asc + search_after(cursor 디코드) + size clamp; 결과→`ProgramSimpleItem`, nextCursor=마지막 id/정렬키, hasNext=size+1 probe 방식 유지) + `DbProgramSearchAdapter`(기존 `ProgramRepositoryCustomImpl` 경로 위임).
- `domain/facility/search/`: 동형. facility는 size=30 고정, 커서 없음, bool should [match(name), match(name.ngram), term(facility_subtype)].
- `ProgramQueryServiceImpl`/`FacilityQueryServiceImpl` 수정: 기존 검색 메서드가 flag 보고 Es/Db 어댑터 위임 + try/catch 계측 fallback(GLOBAL §3). facility `keyword` `required=false`→controller에서 `@NotBlank`+길이 검증 추가(계약상 200 반환 형태 유지, 빈 값은 400).
- `global/search/SearchMetrics`(Micrometer): `search_es_fallback_total{domain}`, `search_es_latency{domain}`(타이머). (리컨실 메트릭은 T4와 공유.)
- 키워드 정규화 parity: program은 `\s+`→"" 적용 후 쿼리.
- **검증**: 로컬 ES 색인된 상태에서 `engine=es`로 `/programs/search?keyword=수영`, `/facilities/search?keyword=수영` 200 + 결과. ES 죽였을 때 fallback + 카운터 증가(통합테스트 T6).
- 모델: **opus**(다중 파일 통합).

### T6 — Testcontainers ES(+Nori) 통합/계약 테스트
**목표**: 안전망(머지 게이트 최소 6종).
- `src/test`에 Testcontainers ES 지원(커스텀 Nori 이미지, `@BeforeAll`에서 `_cat/plugins`로 nori 존재 assert). 대상: ①인젝션-리터럴 처리(`name:(x) OR _exists_:*` → 파싱 안 됨/에러 없음) ②ES-down fallback 계측+로그 ③부분 bulk 실패 표면화 ④리컨실 드리프트 치유 ⑤intent("수영"→수영장 포함, "강남 축구") ⑥contract(DTO 필드 바이트 동일 + search_after 페이지 disjoint). 동등성 회귀는 만들지 않음.
- **검증**: `./gradlew test` 대상 테스트 green(로그 출력).
- 모델: standard(sonnet).

### T7 — 벤치마크(DB vs ES, 완전 동일 환경) + 리포트
**목표**: AS-IS와 같은 형식으로 DB vs ES 비교 리포트.
- app 이미지 재빌드(`docker compose -f perf/docker-compose.yml build app`) → 새 코드 포함. ES 서비스 up + 초기 색인 완료 확인.
- **완전 동일 환경**: 같은 compose 스택/리소스 제한/seed DB/`search_perf.js`/keywords. `engine=db`와 `engine=es`를 app 재기동만으로 스위치(env `MOVEMAP_SEARCH_PROGRAM_ENGINE` 등)하고 **동일 k6 시나리오(같은 MODE/RATE/DURATION)로 back-to-back** 실행. `/programs/search`(주 병목)와 `/facilities/search` 각각.
- 결과 JSON(`perf/out/summary_*_es.json` 등) 수집 + `docker exec` EXPLAIN는 ES엔 없으니 대신 ES `_search` took/pgstat 비교. `perf/ES-VS-DB-REPORT.md` 작성 — `AS-IS-BASELINE-REPORT.md`와 **동일 섹션 구조/표 포맷**, p50/p95/p99/RPS/에러율/자원, 전후 비교, 결론(형태소 품질 정성 포함).
- **검증**: 리포트에 실제 측정 수치가 채워짐(지어내기 금지). 두 조건 모두 k6 종료코드/summary 존재.
- 모델: **opus**(측정 설계·해석).

### T8 — 전체 브랜치 최종 리뷰
- `superpowers:requesting-code-review`의 code-reviewer로 전체 diff 리뷰. Critical/Important는 fix 서브에이전트로.
