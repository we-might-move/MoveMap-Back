import http from 'k6/http';
import { check } from 'k6';
import { SharedArray } from 'k6/data';
import { Trend } from 'k6/metrics';
import { textSummary } from 'https://jslib.k6.io/k6-summary/0.0.2/index.js';

// ============================================================
// ES 직접 호출(앱 우회) 부하 테스트 — p99 꼬리가 ES 엔진(took) 문제인지,
// 앱/JVM/공유호스트 계층 문제인지 isolate하기 위한 스크립트.
//
// 앱의 EsProgramSearchAdapter가 만드는 쿼리와 동일한 바디를 그대로
// http://localhost:9200/program_search/_search 에 POST한다(네이티브 k6, 호스트에서 직접 실행).
//
// 기록하는 두 지표:
//   - http_req_duration : k6 → ES 왕복 시간(네트워크 + ES 엔진 시간)
//   - es_took_ms (Trend) : 응답 바디의 "took"(ES 내부 엔진 처리 시간, ms) — k6/네트워크 오버헤드 제외
//
// 사용 예:
//   k6 run --vus 1 -e RATE=30 -e DURATION=3m perf/k6/es_direct.js
// ============================================================

const ES_URL = __ENV.ES_URL || 'http://localhost:9200/program_search/_search';
const RATE = Number(__ENV.RATE || 30);
const DURATION = __ENV.DURATION || '3m';
const MAX_VUS = Number(__ENV.MAX_VUS || 200);
const PRE_VUS = Number(__ENV.PRE_VUS || 30);

const keywords = new SharedArray('keywords', () => JSON.parse(open('./keywords.json')));

export const es_took_ms = new Trend('es_took_ms', true);

export const options = {
  scenarios: {
    es_direct: {
      executor: 'constant-arrival-rate',
      rate: RATE,
      timeUnit: '1s',
      duration: DURATION,
      preAllocatedVUs: PRE_VUS,
      maxVUs: MAX_VUS,
    },
  },
  summaryTrendStats: ['avg', 'min', 'med', 'p(90)', 'p(95)', 'p(99)', 'max'],
};

function buildBody(kw) {
  return JSON.stringify({
    size: 20,
    query: {
      bool: {
        should: [
          { match: { name: kw } },
          { match: { 'name.ngram': kw } },
          { match: { facility_name: kw } },
        ],
        minimum_should_match: 1,
      },
    },
    sort: [{ id: 'asc' }],
  });
}

// open model: iteration = 요청 1개 (think time 없음 — arrival rate가 페이싱을 통제)
export default function () {
  const kw = keywords[Math.floor(Math.random() * keywords.length)];
  const res = http.post(ES_URL, buildBody(kw), {
    headers: { 'Content-Type': 'application/json' },
    tags: { name: 'es_direct_search' },
  });
  const ok = check(res, { 'status 200': (r) => r.status === 200 });
  if (ok) {
    const took = res.json('took');
    if (typeof took === 'number') {
      es_took_ms.add(took);
    }
  }
}

export function handleSummary(data) {
  const tag = __ENV.COND_TAG || 'es_direct_took';
  return {
    stdout: textSummary(data, { indent: ' ', enableColors: true }),
    [`../out/summary_${tag}.json`]: JSON.stringify(data, null, 2),
  };
}
