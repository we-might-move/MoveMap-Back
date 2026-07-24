#!/usr/bin/env bash
set -euo pipefail
# ============================================================
# k6를 CPU 제한 컨테이너로 실행 (부하 생성기도 격리 → 측정 공정성)
#   - 네이티브 k6는 macOS에서 cgroup CPU 제한이 어려워 컨테이너로 실행
#   - SUT(앱+DB)와 같은 호스트지만, k6에 CPU 상한을 걸어 자원 잠식을 제한
#
# 사용 예:
#   COND_TAG=A_like_run1 ./run.sh
#   COND_TAG=B_pgtrgm_run1 ENDPOINT=/facilities/search K6_CPUS=1 ./run.sh
#
# 조절 env: K6_CPUS(기본 1), K6_MEM(기본 512m), BASE_URL, LOGIN_EMAIL, LOGIN_PW,
#           COND_TAG, ENDPOINT
# ============================================================
PERF_DIR="$(cd "$(dirname "$0")/.." && pwd)"

docker run --rm \
  --network "${K6_NETWORK:-movemap-perf-net}" \
  --cpus="${K6_CPUS:-1}" \
  --memory="${K6_MEM:-512m}" \
  -v "${PERF_DIR}:/perf" -w /perf/k6 \
  -e BASE_URL="${BASE_URL:-http://app:8080}" \
  -e LOGIN_EMAIL="${LOGIN_EMAIL:-perf@test.com}" \
  -e LOGIN_PW="${LOGIN_PW:-Test1234!}" \
  -e COND_TAG="${COND_TAG:-run}" \
  -e ENDPOINT="${ENDPOINT:-/programs/search}" \
  -e MODE="${MODE:-load}" \
  -e RATE="${RATE:-}" \
  -e MAX_RATE="${MAX_RATE:-}" \
  -e DURATION="${DURATION:-}" \
  -e RAMP="${RAMP:-}" \
  -e MAX_VUS="${MAX_VUS:-}" \
  -e PRE_VUS="${PRE_VUS:-}" \
  -e VUS="${VUS:-20}" \
  -e WARMUP="${WARMUP:-30s}" \
  -e MEASURE="${MEASURE:-2m}" \
  -e K6_WEB_DASHBOARD="${K6_WEB_DASHBOARD:-}" \
  -e K6_WEB_DASHBOARD_EXPORT="${K6_WEB_DASHBOARD_EXPORT:-}" \
  grafana/k6:latest run "${SCRIPT:-search_perf.js}"
# 사용 예:
#   MODE=smoke      COND_TAG=smoke      ENDPOINT=/programs/search ./run.sh
#   MODE=load RATE=30 COND_TAG=load30   ENDPOINT=/programs/search ./run.sh
#   MODE=breakpoint MAX_RATE=300 K6_CPUS=3 COND_TAG=bp ENDPOINT=/programs/search ./run.sh
#   (구 closed-model: SCRIPT=search_test.js MEASURE=60s ... ./run.sh)
#   HTML export: K6_WEB_DASHBOARD=true K6_WEB_DASHBOARD_EXPORT=/perf/out/report.html ... ./run.sh

# ── P2(Prometheus remote-write) 추가 시: 위 docker run 에 아래를 더한다 ──
#   -e K6_PROMETHEUS_RW_SERVER_URL="http://host.docker.internal:9090/api/v1/write" \
#   -e K6_PROMETHEUS_RW_TREND_STATS="p(95),p(99),avg,max" \
#   grafana/k6:latest run -o experimental-prometheus-rw --tag testid="${COND_TAG}" search_test.js
