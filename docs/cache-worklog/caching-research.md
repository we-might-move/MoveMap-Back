# 서비스 기업 캐싱 활용처 리서치 (출처 기반)

> 국내 기술 블로그 위주로 캐싱 실제 활용처·패턴을 수집. 각 항목 출처 URL 포함. 미확인은 표시.

## 1. 사례 표

| 회사 | 기능/도메인 | 캐시 계층 | 무엇을 캐싱 | 키/TTL/무효화 | 다룬 패턴 | 성과 | 출처 |
|---|---|---|---|---|---|---|---|
| **우아한형제들(배민)** | 가게노출 시스템(검색/노출 최전방) | Redis 2계층 + DynamoDB 원본 (3-tier) | 시스템 결합 데이터, 시간·위치 기반 계산된 가게 정보 | **무효화 대신 원본→2차Redis→1차Redis 순차 갱신**(캐시 공백 방지). fallback 후 저장은 비동기 | cache-aside + async write, 계층적 fallback | 초당 수십만 조회, ~100 이벤트/초 갱신 | [techblog.woowahan.com/2667](https://techblog.woowahan.com/2667/) |
| **올리브영** | 대규모 증정 행사(읽기 폭증) | **로컬(Caffeine) + Redis(ElastiCache) 2-tier** | 행사 데이터(List), 버전 기반 v1/v2 | Caffeine `expireAfterWrite 60s, max 100`; Redis 버전번호로 로컬 캐시 갱신 판단 | 2-tier, 버전 기반 동기화 | **TPS +478%, Redis 송신량 -99.1%** | [oliveyoung.tech/2024-12-10](https://oliveyoung.tech/2024-12-10/present-promotion-multi-layer-cache/) |
| **토스** | 범용(DB 부하 방지 가이드) | Redis | 조회 결과 + **빈 결과** | TTL **지터(0~10초 랜덤)**; 핫키는 분산락 | **cache stampede→jitter, penetration→negative caching, 캐시장애→failover(핵심/부가 구분), 핫키만료→분산락(1회만 쓰기)** | — | [toss.tech/cache-traffic-tip](https://toss.tech/article/cache-traffic-tip) |
| **화해** | 캐시 스탬피드 대응 | Redis | 조회 결과 | 만료 전 확률적 재계산 | **PER(Probabilistic Early Recomputation)** — `갱신확률=ln(rand())/(delta·λ·TTL)`, 백그라운드 갱신+기존값 반환(응답지연 0) | 동시 미스↓, DB부하 평준화 | [blog.hwahae.co.kr/14003](https://blog.hwahae.co.kr/all/tech/14003) |
| **올리브영** | Redis 운영 꿀팁 | Redis | — | 핫키 대응, TTL 만료 시 원천 쏠림 | 핫키, TTL 관리 | — | [oliveyoung.tech/2025-07-23](https://oliveyoung.tech/2025-07-23/redis-tips-for-developer/) |
| **토스** | 웹 서비스 캐시 다루기 | HTTP/CDN + 서버 | 정적/준정적 응답 | Cache-Control/ETag 등 | HTTP 캐시 계층 | — | [toss.tech/smart-web-service-cache](https://toss.tech/article/smart-web-service-cache) |
| (학술/일반) | 웹 검색엔진 결과 캐싱 | 2-tier(메모리+디스크) | 쿼리 결과, topical | LRU 퇴출 | 2-tier result cache가 디스크 트래픽↓·throughput↑ | — | [arxiv 2001.03010](https://arxiv.org/pdf/2001.03010) |
| (일반) | 자동완성 아키텍처 | Trie + 캐시 + 분산 + **클라이언트 캐싱** | prefix 후보 | 키스트로크 debounce, client-side cache | client-side caching + FST/Trie | — | [dev.to autocomplete](https://dev.to/matt_frank_usa/designing-search-autocomplete-trie-data-structures-at-scale-5f4k) |

## 2. 반복되는 핵심 패턴

1. **Cache-aside(look-aside)** — 미스 시 원천 조회 후 캐시 갱신. 사실상 표준(배민, 토스, 대부분).
2. **Cache Stampede(쇄도) 대응** — 동시 만료로 DB 쏠림. 대응: **TTL 지터**(토스), **PER 확률적 조기 재계산**(화해), **핫키 분산락**(토스, 캐시 미스 시 1회만 쓰기).
3. **Negative caching(빈 결과 캐싱)** — 없는 데이터도 캐싱해 반복 조회 차단(토스, 캐시 관통 방지). 스팸/오타 키워드에 특히 유효.
4. **2-tier(로컬+글로벌)** — Caffeine 로컬 + Redis. 로컬로 네트워크 round-trip·Redis 송신량 절감(올리브영: Redis 송신 -99%). 버전 기반으로 로컬 갱신 판단.
5. **무효화 전략** — (a) TTL 기반, (b) 이벤트/버전 기반(배민 순차 갱신·올리브영 버전번호), (c) **무효화보다 순차 갱신으로 캐시 공백 자체를 없애기**(배민).
6. **Failover(캐시 장애 격리)** — 캐시 죽어도 핵심 기능은 원천으로 동작, 부가 기능만 degrade(토스).
7. **클라이언트 캐싱 + debounce** — 자동완성은 키스트로크마다 발생 → 프론트에서 debounce + 최근 prefix 캐싱이 가장 싼 1차 방어.

## 3. 검색·읽기 캐싱 교훈 요약

- 읽기 위주 + 쿼리 쏠림(핫키) = 캐싱 ROI 최고. 자동완성이 대표.
- 가장 큰 리스크는 "만료 순간의 쏠림(stampede/hot key)" — jitter·PER·락·negative로 방어.
- 무효화가 어려우면 캐싱이 위험. 반대로 **데이터가 드물게/정해진 시점에만 바뀌면(배포/버전) 무효화가 거의 공짜** → 캐싱 최적 조건.
- 2-tier(로컬+Redis)는 Redis 자체가 핫/네트워크 병목일 때 큰 효과(올리브영 -99% 송신).
