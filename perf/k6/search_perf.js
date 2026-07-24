import http from 'k6/http';
import { check } from 'k6';
import { SharedArray } from 'k6/data';
import { textSummary } from 'https://jslib.k6.io/k6-summary/0.0.2/index.js';

// ============================================================
// open model(arrival-rate) 기반 검색 부하 테스트
//   MODE=smoke|load|breakpoint  (기본 load)
//   - smoke : constant-arrival-rate RATE(기본 5) RPS, DURATION(기본 3m)
//   - load  : constant-arrival-rate RATE(기본 30) RPS, DURATION(기본 10m)
//   - breakpoint : ramping-arrival-rate 10 → MAX_RATE(기본 300) RPS, RAMP(기본 20m)
//                  SLO 위반 시 자동 중단(abortOnFail) → 그 지점이 포화점
// 공통 env: ENDPOINT, VUS 상한(MAX_VUS), PRE_VUS, LOGIN_*
//
// ⚠️ 부하기(k6 컨테이너)가 병목이 되지 않게 주의:
//   - 높은 RPS 테스트는 K6_CPUS를 넉넉히(예: 2~3) 주고,
//   - 결과의 dropped_iterations 가 크면 "부하기 포화" 신호 → SUT 성능 아님.
// ============================================================
const MODE = __ENV.MODE || 'load';
const ENDPOINT = __ENV.ENDPOINT || '/programs/search';
const BASE = __ENV.BASE_URL || 'http://app:8080';

const RATE = Number(__ENV.RATE || (MODE === 'smoke' ? 5 : 30));
const MAX_RATE = Number(__ENV.MAX_RATE || 300);
const DURATION = __ENV.DURATION || (MODE === 'smoke' ? '3m' : '10m');
const RAMP = __ENV.RAMP || '20m';
const MAX_VUS = Number(__ENV.MAX_VUS || 800);
const PRE_VUS = Number(__ENV.PRE_VUS || 50);

const keywords = new SharedArray('keywords', () => JSON.parse(open('./keywords.json')));

function buildScenario() {
  if (MODE === 'breakpoint') {
    return {
      executor: 'ramping-arrival-rate',
      startRate: 10,
      timeUnit: '1s',
      preAllocatedVUs: PRE_VUS,
      maxVUs: MAX_VUS,
      stages: [{ target: MAX_RATE, duration: RAMP }],
    };
  }
  return {
    executor: 'constant-arrival-rate',
    rate: RATE,
    timeUnit: '1s',
    duration: DURATION,
    preAllocatedVUs: PRE_VUS,
    maxVUs: MAX_VUS,
  };
}

const isBreakpoint = MODE === 'breakpoint';

export const options = {
  scenarios: { search: buildScenario() },
  thresholds: {
    // breakpoint 모드에선 SLO 위반 시 테스트를 멈춰 그 지점을 포화점으로 기록
    http_req_duration: [{ threshold: 'p(99)<1000', abortOnFail: isBreakpoint, delayAbortEval: '10s' }],
    http_req_failed: [{ threshold: 'rate<0.01', abortOnFail: isBreakpoint, delayAbortEval: '10s' }],
  },
  summaryTrendStats: ['avg', 'min', 'med', 'p(90)', 'p(95)', 'p(99)', 'max'],
};

export function setup() {
  const res = http.post(
    `${BASE}/auth/login`,
    JSON.stringify({ email: __ENV.LOGIN_EMAIL, password: __ENV.LOGIN_PW }),
    { headers: { 'Content-Type': 'application/json' } }
  );
  check(res, { 'login 200': (r) => r.status === 200 });
  const token = res.json('accessToken');
  if (!token) throw new Error(`로그인 실패: status=${res.status} body=${res.body}`);
  return { token };
}

// open model: iteration = 요청 1개 (think time 없음 — arrival rate가 페이싱을 통제)
export default function (data) {
  const kw = keywords[Math.floor(Math.random() * keywords.length)];
  const res = http.get(`${BASE}${ENDPOINT}?keyword=${encodeURIComponent(kw)}`, {
    headers: { Authorization: `Bearer ${data.token}` },
    tags: { name: 'search' },
  });
  check(res, { 'status 200': (r) => r.status === 200 });
}

export function handleSummary(data) {
  const tag = __ENV.COND_TAG || `${MODE}_run`;
  return {
    stdout: textSummary(data, { indent: ' ', enableColors: true }),
    [`../out/summary_${tag}.json`]: JSON.stringify(data, null, 2),
  };
}
