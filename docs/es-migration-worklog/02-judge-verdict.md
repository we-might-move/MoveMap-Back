# 02 — Adversarial Judge Verdict: MoveMap ES Autocomplete Migration

> Role: adversarial judge over Plan A (Lean), Plan B (Robust), Plan C (Security & Correctness).
> Method: steelman each side per axis, find where each is *wrong*, rule with a confidence %, then synthesize one blended plan.
> Fixed context: D1=A (autocomplete only). Measured bottleneck = `/programs/search` collapsing at ~30 RPS (228만-row scan); `/facilities/search` already fast (p95 33–39ms). No runtime write path for facility/program rows (`facts §4`). No `created_at/updated_at` on those tables (`facts §3`). Facility keyword search is case-sensitive `LIKE` and `keyword` is `required=false` (`facts §1-2`).

---

## A. Debate table

| # | Axis | Plan A (Lean) | Plan B (Robust) | Plan C (Security) | Strongest counter-argument | RULING (confidence) |
|---|------|---------------|-----------------|-------------------|----------------------------|---------------------|
| 1 | **Sync given no write path / D7=Outbox** | Reject Outbox entirely; bulk index + `@Scheduled` reconcile (count+MAX(id)) | Build Outbox+relay+DLQ+ShedLock **dormant** + V18 timestamps; reconcile is today's real guarantee | Build outbox **table + one publish hook** dormant, minimal; reindex + reconciliation is the real guarantee | An Outbox with no producer is untestable-against-real-writes ceremony; but count+MAX(id) misses in-place UPDATEs (`V14.3`) — a real hole | **OVERRIDE D7.** Today's guarantee = initial reindex + **content-hash reconciliation** + **reindex-on-migration hook**. Ship a documented *one-method* dormant seam (no relay worker / no DLQ / no ShedLock now). Blend = C's minimalism + A's honesty. **85%** |
| 2 | **Managed vs self-host** | Managed (Elastic Cloud), self-host only local (60%) | Self-host single node — the ops demo *is* the deliverable (70%) | Managed — solo operator hardening ES8 is the top breach source (70%) | The ADR's own stated goal is *operability learning*; managed hides exactly that story. But a public/unhardened self-hosted ES8 is the single likeliest real hole | **Self-host ONE node via docker-compose (reuse `perf/` compose) for the portfolio demo + dev/test; recommend managed for "real prod" in the README.** Security ON, bound private/localhost, never `9200` public. Show the ops, state you'd buy managed in prod. **65%** |
| 3 | **Does facility need ES?** | Index it (morphology + FIXME), but self-critique admits program-only is better (55%) | Index both for symmetry; flag can leave facility on PG indefinitely | Index both; fixes case-bug via `lowercase` filter | Facility is already fast; its only real defects (case-sensitive `LIKE`, multi-token "강남 축구") are **cheaply fixable in PG**. ES for facility is a morphology/consistency choice, not a perf one | **Program-ONLY through P1–P3** (the measured bottleneck). Fix facility's case-sensitivity in PG (`LIKE`→`ILIKE`) as a free P0 win. Index facility in ES only in a later phase, labeled morphology/consistency (not perf). **70%** |
| 4 | **V18 add created_at/updated_at?** | No — coarse count/MAX(id) | **Yes** — needed for delta reconcile + external versioning + triggers | No new columns — uses `content_hash` + `source_updated_at` mirror | External versioning only matters under concurrent out-of-order writes — which only exist with the (future) Outbox. Deterministic `_id` + full overwrite is already idempotent today | **Skip V18.** `content_hash` (C) catches in-place edits that both count/MAX(id) *and* a stale `updated_at` would miss, with zero schema churn on write-less tables. Revisit timestamps the day admin-CRUD + Outbox land. **70%** |
| 5 | **ES client** | Pure Spring Data ES + escape hatches | Hybrid: Spring Data dep, low-level `ElasticsearchClient` for bulk/versioning | Hybrid: Spring Data + native client for injection-safe queries | Per-item `BulkResponse.errors()` inspection is impossible through Spring Data `saveAll`; missing it = silent partial-index failure (a real correctness hole) | **Hybrid (B/C).** Spring Data ES as dependency + use `ElasticsearchClient` directly for bulk (must check `resp.errors()`) and typed query building. **85%** |
| 6 | **Autocomplete query mechanism** | completion (page 1) + `match_phrase_prefix` (page N) — flags own recall inconsistency | edge_ngram multifield + `match` + `search_after` (uniform) | `match`/`match_phrase_prefix` + completion for dropdown | Program search **is paginated**; a page-1/page-N mechanism split (A) means a "더보기" can surface docs absent from page 1 or vice-versa — a visible correctness defect | **One mechanism across all pages for program: edge_ngram (or `search_as_you_type`) + `match` + `search_after` + `id` tie-breaker.** This **overrides design D5's "completion suggester confirmed."** Completion suggester is optional for a *non-paginated* dropdown / facility only. **70%** |
| 7 | **Injection/validation + facility `required=false` bug** | Brief; add ≤100 cap while here | Length caps at controller; typed builders = no injection | **Centerpiece:** `required=true`, length caps, control-char reject, size clamp, never `query_string` | With typed `match` builders, Lucene injection is already foreclosed for *all three* — the "hardening" is mostly *not doing the wrong thing* + cheap input validation | **Adopt C's input validation (essential, ~0.5d): fix `required=false`→required, add length caps + size clamp + control-char reject.** Typed-builder injection-safety is free. Not gold-plating; it fixes a real NPE/whole-scan bug. **90%** |
| 8 | **Observability depth** | Single fallback counter | 7 Micrometer metrics (incl. relay lag/dead) | Metered fallback essential + drift + freshness gauges | A's single counter is too thin (no latency, no drift); B's 7 meter *dormant* components (relay lag/dead are always 0 today) | **~4 metrics that measure something real today:** fallback counter, ES query latency timer, reconciliation drift gauge, reconciliation last-success timestamp. Add outbox metrics *with* the outbox. **80%** |
| 9 | **Effort / ambition** | ~2 wk | 17–23 d | 14–17 d | Portfolio must read senior-but-not-over-engineered; B's dormant outbox/DLQ/V18 is the "over-engineering" a reviewer flags; A's sync is too coarse | **Target ≈ 9–13 dev-days** = A's skeleton + C's correctness rigor (bulk-error check, content-hash reconcile, validation, metered fallback) − B's dormant outbox/DLQ/V18. **75%** |
| 10a | **Review-search inclusion** | Exclude (85%) | Exclude (85%) | Exclude — PII/erasure (85%) | None credible | **Exclude.** Not in the bottleneck, not in the A-scope DTOs, and reviews have a live write path that would force the Outbox question early. **88%** |
| 10b | **D4 dictionary curation** | Empty→semi-auto (70%) | Semi-auto + manual gate (75%) | Semi-auto + human approve (65%) | None credible | **Start `mixed` + small/empty `userdict_ko.txt`; semi-auto candidate extraction from name tokens, human-gated; dict change = reindex via alias.** **72%** |
| 10c | **Nori POS stoptags (XPN)** | Keep default stoptags (audit P4) | Keep default (audit P4) | **Remove XPN** so 비회원/무산소/비급여 don't invert | Default stoptags drop XPN(접두사) → documented meaning-reversal; the domain has these terms | **Remove XPN from stoptags now (C).** Cheap, correctness-positive, backed by Elastic's own warning. **65%** |
| 10d | **Mapping `dynamic`** | (unspecified) | (unspecified) | `dynamic: strict` | Silent creation of an un-searchable field on schema drift is exactly the silent-hole class to avoid | **Adopt `dynamic: strict` (C).** Free correctness win. **80%** |

---

## B. Where each plan is wrong (biggest error / blind spot)

**Plan A (Lean) — wrong points:**
1. **Reconciliation by `COUNT(*)` + `MAX(id)` is too coarse — a real drift hole.** It cannot detect an in-place UPDATE that changes a `name` without changing row count or max id — and `V14.3/V14.4` are UPDATE-heavy migrations (`facts §4`). A admits this in §11.4 but still ships it as the v1 mechanism. Fix: content-hash compare (borrow from C).
2. **Page-1 completion vs page-N `match_phrase_prefix` recall split is a shipped UX defect,** not a benign "conscious difference." Program search is paginated; two mechanisms mean "더보기" can disagree with page 1.
3. **No `BulkResponse.errors()` per-item check.** This is the one place lean-ness crosses into a genuine correctness hole: a bulk returns HTTP 200 with failed items → silent partial index. Cheap to fix, must not be skipped.
4. **Internal inconsistency:** argues facility doesn't need ES, then indexes it anyway; its own self-critique (55% confidence) says program-only is better. It should have committed to program-only.

**Plan B (Robust) — wrong points:**
1. **Builds Outbox + relay + DLQ + ShedLock for a producer that does not exist** (`facts §4`) — 6–8 days of code that cannot be exercised by a real write and half-concedes as over-engineering. The seam is worth *documenting*; the machinery is not worth *building* now.
2. **V18 `created_at/updated_at` + triggers on write-less tables is schema churn for near-zero present benefit.** `content_hash` achieves reconciliation without it; external versioning it unlocks only matters under the (nonexistent) concurrent-write path.
3. **`edge_ngram min_gram: 1`** indexes every single character across 228k docs → index bloat + noisy single-char matches. This is a concrete tuning error (min_gram 2 is the sane floor).
4. **7-metric suite meters dormant components** (`search_index_lag`, `search_relay_dead` are constantly 0/absent today) — dashboard ceremony.
5. **`nori_readingform` (romaja) in the index analyzer** adds tokens of dubious value for Korean facility-name autocomplete and can inject noise; unjustified for scope A.

**Plan C (Security & Correctness) — wrong points:**
1. **Mixed message on the Outbox:** the plan body (§5a #3) still ships `outbox` table + `OutboxRelayWorker` + `BulkIndexer`, then §11 self-critique correctly says *don't build the worker*. The self-critique is right; the plan should have cut it from the body. A judge reading only §5 would over-build.
2. **Managed-ES recommendation is security-optimal but throws away the portfolio's stated ADR goal** (operability learning). C doesn't grapple with that tension as squarely as B does — it optimizes for one lens (attack surface) and under-weights the demo narrative that is the project's actual purpose.
3. **`edge_ngram min_gram: 1`** — same bloat/noise error as B.
4. **9 mandatory merge-gate tests for a solo portfolio** risks aspiration that never gets written; the *right* subset (injection, metered fallback, bulk-partial-fail, reconciliation-heals, intent, contract) is ~6 and should be named as the floor.
5. **Nightly full checksum over 228k rows** is likely over-cautious once `reindex-on-migration` already rebuilds on every data change; delta + count is enough at this size.

---

## C. Synthesized "best blend" plan (phased, decisive)

**Spine:** program-only ES migration, hybrid client, uniform edge_ngram+`search_after`, content-hash reconciliation + reindex-on-migration (no live Outbox), metered fallback, self-hosted single node for the demo. ~9–13 dev-days.

### P0 — Safety net + free PG wins (0.5–1 d)
- Document current `/search` behavior; scaffold intent + contract tests (Testcontainers dep already declared, `facts §5/§7`).
- **Fix facility `keyword required=false`→ required + add length cap** at the controller edge (fixes NPE/whole-scan risk, `facts §1`). **Confidence 90%.**
- **Fix facility case-sensitivity in PG now:** `LIKE`→`ILIKE` (`FacilityRepository.java:16`), independent of ES. Free correctness win. **Confidence 85%.**

### P1 — ES infra, program-first (2–3 d)
- `spring-boot-starter-data-elasticsearch` (Boot 3.5.7 BOM manages Spring Data ES 5.x + ES java client 8.x). **Hybrid client** — Spring Data dep, `ElasticsearchClient` used directly for bulk + query building. **Confidence 85%.**
- `ElasticsearchConfig` (bounded 2s/5s timeouts → fast fallback; TLS + basic-auth/API-key from a **gitignored** `application-es.yml` per `facts §6`).
- Single-node **self-hosted** ES + Nori via docker-compose (reuse `perf/` compose pattern), security ON, bound private. **Confidence 65%.**
- `program_v1` mapping: `dynamic: strict`; `nori mixed` (D3); **XPN removed from POS stoptags**; `name`/`facility_name` text(nori)+`keyword`; `name.ngram` edge_ngram **min_gram:2** (not 1); `content_hash` + `source_updated_at` internal fields (never serialized out); alias `program_search`. **Confidence 75%.**
- `IndexBootstrapper` (idempotent create index+alias).

### P2 — Indexing / sync, no live Outbox (2–3 d)
- `BulkReindexer`: keyset-stream PG (`id > ? ORDER BY id LIMIT 5000`) → bulk into `*_vN`; **check `BulkResponse.errors()` per item** → log + `bulkItemFailures` metric + bounded retry → dead-letter (start as "log + manual re-run"). **Confidence 90% this check is mandatory.**
- **Reconciliation `@Scheduled` (15 min):** content-hash delta compare + `count(PG)` vs `count(ES)`; auto-heal diverged ids; emit `reconciliation_drift` + `last_success_timestamp`. **This is today's real correctness guarantee.** **Confidence 85%.**
- **Reindex-on-migration hook** (Flyway `afterMigrate` or runbook gate) → rebuild + atomic alias swap. Closes the "new V-script inserted rows, ES never learned" path. **Confidence 80%.**
- **Outbox = documented one-method dormant seam only** (`SearchIndexEvents.publish(...)` that future CRUD must call in-tx). **No relay worker, no DLQ, no ShedLock, no V18 now.** **Confidence 85%.**

### P3 — Program read path + rollout (2–3 d)
- `ProgramSearchPort` + `EsProgramSearchAdapter` (typed builders, injection-safe) + `DbProgramSearchAdapter` (existing native path = fallback). ES document type never leaks past the port (C's boundary rule). **Confidence 85%.**
- **Uniform paging:** `match` on `name`/`facility_name` + `name.ngram` + `search_after` + `id` tie-breaker across *all* pages (overrides D5 completion-only). Whitespace-strip parity applied to the query term. **Confidence 70%.**
- Input validation (length cap, size clamp, control-char reject, opaque server-validated cursor).
- Per-domain feature flag (default DB) + **metered fallback**: WARN (log `keyword_len`, not raw term) + `esFallback` counter. Cut over **program only**. **Confidence 85%.**

### P4 — Tests, tune, quantify (2–3 d)
- Testcontainers ES+Nori (custom image; assert plugin present in `@BeforeAll`). **Test floor (6):** injection-as-literal, fallback-is-metered+logged, bulk-partial-failure-surfaced, reconciliation-heals-drift, intent ("수영"→수영장; "강남 축구"), contract (DTO byte-identical across DB/ES). **Confidence 80%.**
- Re-run 30 RPS benchmark to quantify the bottleneck removal (the whole point).
- Dictionary/POS tuning; **then optionally** index facility for morphology (labeled non-perf) or defer.

**Metrics (4):** `esFallback{domain}`, `es_search_latency{domain}`, `reconciliation_drift`, `reconciliation_last_success_ts`.

---

## D. Portfolio framing guidance (narrate as senior judgment)

**1. The D7=Outbox override — frame as evidence-over-authority, not indecision.**
> "The design doc confirmed a Transactional Outbox. Verifying the codebase (`facts §4`) showed there is **no runtime write path** for facility/program rows — they are Flyway-loaded; the only live `@Transactional` mutations are bookmark/review toggles that never touch indexable fields. An Outbox with no producer is ceremony you can't even test against a real write. So I named the real invariant — *PostgreSQL is Source-of-Truth, ES is a derived index, staleness is bounded by the reconciliation interval* — made the mechanism match how data actually changes today (**initial reindex + content-hash reconciliation + reindex-on-migration**), and left a documented one-line seam so the Outbox activates the day admin-CRUD ships. Overriding a 'confirmed' decision on evidence, and saying exactly when the deferred design flips on, is the senior move."

**2. "Facility already fast" — never claim ES sped it up (the measurement refutes it).**
> "The measured bottleneck was `/programs/search` — a 228만-row scan collapsing at 30 RPS. `/facilities/search` was already p95 33–39ms, so it stayed on PostgreSQL. Its only real defects were a case-sensitive `LIKE` and weak multi-token matching ('강남 축구'), which I fixed cheaply in PG. Bringing facility onto ES later is a *morphology/consistency* choice, labeled as such — not a performance claim. Justifying ES by speed here would be dishonest and self-refuting."

**3. Managed vs self-host — state the tension openly.**
> "I self-hosted a single ES node to demonstrate the operations that are this migration's real complexity — versioned indices, atomic alias swaps, reindex-on-dictionary-change, snapshots. In production I'd run managed (Elastic Cloud): a solo operator hardening ES8 (cert rotation, network isolation) is the likeliest source of an actual breach, and managed ships Nori as a supported plugin. The portfolio shows the ops I *can* run; the recommendation shows I know *when not to.*"

---

## E. Residual risks / least-sure rulings (honest self-assessment)

1. **Managed vs self-host (Axis 2, 65%)** — genuinely narrative-dependent. I can't see how the portfolio owner weights "ops demo" vs "smallest attack surface." If the piece is pitched to infra/SRE reviewers, self-host wins; to security reviewers, managed. My split-the-difference (self-host demo + managed recommendation) hedges but isn't provably optimal.
2. **Facility on ES at all vs PG-fix-only (Axis 3, 70%)** — hinges on how much the "강남 축구" multi-token morphology story matters to the owner. If it's a headline feature, index facility sooner.
3. **Autocomplete mechanism (Axis 6, 70%)** — I'm overriding a "confirmed" D5 (completion suggester) on *reasoning about pagination consistency*, not measurement. edge_ngram vs `search_as_you_type` vs completion genuinely needs a real-data A/B. Least-confident override in the verdict.
4. **Skipping V18 timestamps (Axis 4, 70%)** — if admin-CRUD is imminent on the roadmap (I can't see the roadmap), B's build-ahead becomes materially more defensible and content_hash alone becomes the weaker choice.
5. **Nori Testcontainers image packaging** — design flagged this "확인 필요"; still **not empirically confirmed** here (I did not build the image). Assert plugin presence in `@BeforeAll` before trusting any intent test.
6. **`min_gram:2` and XPN-stoptag removal** are reasoned defaults, not measured — validate against real facility/program name samples in P4.

> Overall confidence the blended spine (program-only, hybrid client, content-hash reconcile + reindex-on-migration, no live Outbox, uniform search_after, metered fallback, ~9–13 d) is the right ambition level for this portfolio: **~78%.**
