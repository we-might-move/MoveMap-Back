import http from 'k6/http';
import { check, sleep } from 'k6';
import { SharedArray } from 'k6/data';
import { textSummary } from 'https://jslib.k6.io/k6-summary/0.0.2/index.js';

// 실행 예:
//   BASE_URL=http://localhost:8080 LOGIN_EMAIL=perf@test.com LOGIN_PW='Test1234!' \
//   COND_TAG=A_like_run1 k6 run search_test.js
// (P2에서 remote-write 추가)

const BASE = __ENV.BASE_URL || 'http://localhost:8080';
const ENDPOINT = __ENV.ENDPOINT || '/programs/search'; // /facilities/search 등으로 교체 가능

// 검색어를 1회만 로드해 전 VU가 공유(메모리 절약)
const keywords = new SharedArray('keywords', () => JSON.parse(open('./keywords.json')));

const VUS = Number(__ENV.VUS || 20);
const WARMUP = __ENV.WARMUP || '30s';   // 워밍업(분석 시 제외)
const MEASURE = __ENV.MEASURE || '2m';  // 본 측정

export const options = {
  scenarios: {
    search: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: WARMUP,  target: VUS }, // ① 워밍업
        { duration: MEASURE, target: VUS }, // ② 본 측정
        { duration: '10s',   target: 0 },   // ③ 쿨다운
      ],
    },
  },
  thresholds: {
    // SLO: 위반 시 k6가 실패코드로 종료(CI 게이팅). 첫 A 측정값을 보고 조정.
    http_req_duration: ['p(95)<300', 'p(99)<500'],
    http_req_failed: ['rate<0.01'],
  },
  // 요약/JSON에 p50·p90·p95·p99 모두 포함 (기본은 p90/p95만)
  summaryTrendStats: ['avg', 'min', 'med', 'p(90)', 'p(95)', 'p(99)', 'max'],
};

// 토큰 1회 발급 → 전 VU 공유 (측정 지표에서 제외됨)
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

export default function (data) {
  const kw = keywords[Math.floor(Math.random() * keywords.length)];
  const res = http.get(`${BASE}${ENDPOINT}?keyword=${encodeURIComponent(kw)}`, {
    headers: { Authorization: `Bearer ${data.token}` },
    tags: { name: 'search' },
  });
  check(res, { 'status 200': (r) => r.status === 200 });
  sleep(1); // think time
}

// 결과를 파일로 저장 (조건·회차별로 파일명 분리 → 누적)
export function handleSummary(data) {
  const tag = __ENV.COND_TAG || 'run';
  return {
    stdout: textSummary(data, { indent: ' ', enableColors: true }),
    [`../out/summary_${tag}.json`]: JSON.stringify(data, null, 2),
  };
}
