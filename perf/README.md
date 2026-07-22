# perf/ — 검색 성능 측정 (P1: Minimal)

현재 검색(PostgreSQL `LIKE`/`ILIKE`)의 성능 baseline을 측정하고, ES 전환 효과를 수치로 비교하기 위한 도구 모음.
자세한 방법론은 설계 문서(`movemap-perf-monitoring-design.md`, `performance-measurement-primer.md`) 참고.

## 측정 대상 3조건
- **A** = 현재(텍스트 인덱스 없음, `%kw%` Seq Scan)
- **B** = pg_trgm GIN 인덱스 추가
- **C** = ES (별도 전환 후, 같은 스크립트로 `COND_TAG`만 바꿔 실행)

## 측정 환경 (반드시 결과와 함께 명시)

이 벤치마크는 **prod 환경이 아니라, 리소스를 제한한 컨테이너 환경**에서의 상대 비교다. 결과 보고 시 아래를 함께 적는다.

| 항목 | 값 (예시 — 실제 실행값으로 채울 것) |
|---|---|
| 호스트 | Apple Silicon Mac / colima VM (4 CPU, 8GB, arm64) |
| DB 컨테이너 제한 | **2 CPU / 2GB** (`deploy.resources.limits`) |
| Redis 컨테이너 제한 | 0.5 CPU / 256MB |
| k6(부하기) 제한 | **1 CPU / 512MB** (컨테이너 실행) |
| 앱(Spring) | macOS 네이티브 실행 (⚠️ 컨테이너 아님 = 리소스 미제한) |
| DB 이미지 | `postgis/postgis:16-3.4` (arm64에서 amd64 에뮬레이션 — 절대수치 왜곡 가능, 상대비교는 유효) |
| 부하기 위치 | SUT와 **같은 호스트**(별도 서버 아님) — CPU 제한으로 잠식 완화 |

> 정직한 한계: ① 앱은 네이티브라 리소스 미제한, ② 부하기와 SUT가 같은 물리 호스트, ③ colima VM 2겹 가상화. → "독립 서버"가 아니라 **"리소스 제한 컨테이너 환경에서의 A/B/C 상대 비교"** 로 서술한다.

## 디렉토리
```
perf/
├── docker-compose.yml         # 측정용 Postgres(+Redis). P2에서 prometheus/grafana 주석 해제
├── postgres/initdb/           # pg_stat_statements·pg_trgm·postgis 확장(최초 1회)
├── k6/search_test.js          # k6 부하 스크립트
├── k6/keywords.json           # 고정 검색어셋(길이 섞음)
├── sql/A_no_index.sql         # 조건 A 복귀(인덱스 제거)
├── sql/B_pg_trgm.sql          # 조건 B(trigram 인덱스 생성)
├── sql/explain_queries.sql    # EXPLAIN·pg_stat_statements 조회
└── out/                       # k6 결과 JSON 저장
```

## 사전 준비
- Docker, k6(`brew install k6`), psql
- 앱은 로컬 `develop` 프로파일 설정(application-develop.yml)이 있다고 가정

## 실행 순서

### 1) 측정용 DB 기동
```bash
cd perf
docker compose up -d postgres redis
docker logs -f movemap-perf-db      # "database system is ready" 확인
```

### 2) 앱을 측정용 DB에 붙여 기동 (시드 자동 적재)
프로젝트 루트에서:
```bash
./gradlew bootRun --args='--spring.profiles.active=develop,perf'
```
- `develop`이 시크릿/dialect 제공, `perf`가 datasource를 측정용 DB로 오버라이드.
- Flyway가 시드(facility ~5.5만 / program ~22.8만) 자동 적재.
- 부팅 후 통계 갱신:
  ```bash
  psql "postgresql://movemap:movemap@localhost:5432/movemap" -c "ANALYZE facility; ANALYZE program;"
  ```

### 3) 측정 계정 생성 (JWT)
```bash
curl -X POST http://localhost:8080/auth/signup -H 'Content-Type: application/json' \
  -d '{"email":"perf@test.com","password":"Test1234!","nickname":"perf"}'   # SignupRequest 필드에 맞춰 조정
curl -X POST http://localhost:8080/auth/login  -H 'Content-Type: application/json' \
  -d '{"email":"perf@test.com","password":"Test1234!"}'                      # accessToken 나오는지 확인
```

### 4) 조건 A 측정
```bash
# (A로 초기화 — trigram 인덱스 제거)
psql "postgresql://movemap:movemap@localhost:5432/movemap" -f sql/A_no_index.sql

# DB 통계 리셋
psql "postgresql://movemap:movemap@localhost:5432/movemap" -c "SELECT pg_stat_statements_reset();"

# k6 부하 — CPU 제한 컨테이너로 실행 (perf/k6/run.sh)
cd k6
COND_TAG=A_like_run1 ENDPOINT=/programs/search ./run.sh
# 결과: ../out/summary_A_like_run1.json + 콘솔에 p50/p95/p99·RPS·에러율
# 조절: K6_CPUS(기본 1) K6_MEM(기본 512m) 로 부하기 제한 변경 가능
```
> run.sh는 k6를 `grafana/k6` 컨테이너로 `--cpus`/`--memory` 제한을 걸어 실행한다(네이티브 k6는 macOS에서 CPU 제한이 어려움). 앱은 컨테이너 안에서 `host.docker.internal:8080`으로 접근한다.
부하 도는 동안 다른 터미널에서 자원 스냅샷:
```bash
docker stats --no-stream movemap-perf-db
```

### 5) DB 레벨 증거 캡처
```bash
psql "postgresql://movemap:movemap@localhost:5432/movemap" -f sql/explain_queries.sql
# Seq Scan 여부 / actual time / Buffers hit·read / pg_stat_statements mean_ms 기록
```

### 6) 조건 B 측정
```bash
psql "postgresql://movemap:movemap@localhost:5432/movemap" -f sql/B_pg_trgm.sql
# 4)~5) 반복하되 COND_TAG=B_pgtrgm_run1
# 기대: EXPLAIN이 Bitmap Index Scan으로 바뀌고 p95·mean_ms 감소
```

### 7) 결과 정리
조건 × (cold/warm) × 3회 → 중앙값으로 표 작성:

| 조건 | 엔드포인트 | k6 p50 | k6 p95 | k6 p99 | RPS | 에러율 | pg mean_ms | EXPLAIN 계획 | DB CPU |
|---|---|---|---|---|---|---|---|---|---|
| A | /programs/search | | | | | | | Seq Scan | |
| B | /programs/search | | | | | | | Bitmap Index Scan | |

## 주의 / 함정
- `pg_stat_statements`는 `shared_preload_libraries`가 필요 → compose에 이미 설정됨(컨테이너 최초 생성 시 적용).
- 앞 30초는 워밍업 → 분석에서 제외. cold/warm 구분해 라벨.
- 1~2글자 검색어("강","수")는 pg_trgm 효율이 낮음 → 그 차이를 keywords.json으로 관찰.
- **속도만** 측정. 검색 품질(recall/precision)은 별도 골든셋 오프라인 측정 필요.

## 다음 단계 (P2)
`docker-compose.yml`의 prometheus/grafana/postgres_exporter 주석을 풀고, 앱에 actuator+micrometer 추가 → Grafana 대시보드로 3조건 오버레이. (구현 가이드 P2 절 참고)
