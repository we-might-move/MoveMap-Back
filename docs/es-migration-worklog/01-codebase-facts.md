# MoveMap-Back Search Implementation — Codebase Facts

Repo root (worktree): `/Users/onam-ui/Desktop/취업/Portfolio/MoveMap-Back/.claude/worktrees/perf-baseline-p1`
Base package: `org.wemightmove.movemap`

---

## 1. Search endpoints

### `GET /facilities/search` — FacilityController
File: `src/main/java/org/wemightmove/movemap/domain/facility/controller/FacilityController.java:170-184`
```java
@GetMapping("/search")
public ResponseEntity<FacilitySimpleListResponse> searchFacilityListByKeyword(
        @AuthenticationPrincipal CustomUserDetails member,
        @RequestParam(value = "keyword", required = false) String keyword) {
    Long memberId = member.getId();
    FacilitySimpleListResponse response = facilityQueryService.searchFacilityListByKeyword(memberId, keyword);
    return ResponseEntity.ok(response);
}
```
- Auth: `@AuthenticationPrincipal` required (no whitelist entry → JWT mandatory, see §6).
- No pagination params at all (no cursor/size) — capped by `LIMIT 30` in SQL (see §2).
- Has a `FIXME` comment right above it (line 167-169): *"강남 축구 -> 이런 식으로 검색해도 잘 나오도록 개선하기"* (make multi-token/compound searches like "강남 축구" work well) — direct evidence motivating an ES migration.
- Response DTO: `FacilitySimpleListResponse` (`.../facility/dto/response/FacilitySimpleListResponse.java`), record with `List<FacilitySimpleInfo>`; each item: `id, name, facilityType, facilitySubtype, address`. No pagination fields, no score, no distance.

### `GET /programs/search` — ProgramController
File: `src/main/java/org/wemightmove/movemap/domain/program/controller/ProgramController.java:216-225`
```java
@GetMapping("/search")
public ResponseEntity<ProgramSimpleListResponse> searchPrograms(
        @AuthenticationPrincipal CustomUserDetails member,
        @Valid @ModelAttribute ProgramSearchByKeywordRequest request) {
    Long memberId = member.getId();
    ProgramSimpleListResponse response = programQueryService.searchPrograms(memberId, request);
    return ResponseEntity.ok(response);
}
```
- Auth: same as above, JWT-required.
- Request DTO: `ProgramSearchByKeywordRequest` (record) — `keyword` (`@NotBlank`, 1-100 chars), `size` (1-100, default 20 via compact constructor), `cursor` (nullable, `<=0` coerced to null). Has cursor-based pagination (unlike facility search).
- Response DTO: `ProgramSimpleListResponse` — `List<ProgramSimpleItem>{id, programName, facilityName, facilitySubtype, address}` + `nextCursor`, `hasNext`, `currentSize`.
- Swagger doc (lines 192-215) explicitly documents: "공백 무시" (whitespace ignored), "Prefix 검색", "프로그램명 + 시설명 모두 검색", "ID 오름차순", cursor semantics "커서는 마지막 ID 이상부터 조회" (note: actual SQL uses `id > :cursor`, ASC order — see §2).

**Asymmetry note**: Facility search has no pagination and a hardcoded LIMIT 30; Program search has full cursor pagination, prefix-matching, and whitespace normalization. These are NOT parallel implementations.

---

## 2. Repository layer — the actual keyword queries

### Facility keyword search — `FacilityRepository` (Spring Data JPA `@Query`, native, NOT QueryDSL)
File: `src/main/java/org/wemightmove/movemap/domain/facility/repository/FacilityRepository.java:16-17`
```java
@Query(value = "SELECT * FROM facility f WHERE f.name LIKE CONCAT('%', :keyword, '%') OR f.facility_subtype LIKE CONCAT('%', :keyword, '%') LIMIT 30", nativeQuery = true)
List<Facility> searchFacilitiesByNameAndFacilitySubtype(@Param("keyword") String keyword);
```
- Uses `LIKE` (NOT `ILIKE` — case-sensitive), substring match (`%kw%`) on both sides — cannot use any B-tree index, always full/seq scan candidate.
- No normalization (no whitespace stripping) applied to `keyword` before this call — confirmed by `FacilityQueryServiceImpl.searchFacilityListByKeyword` (line 105-112) passing `keyword` straight through with no transform.
- Hardcoded `LIMIT 30`, no cursor, no ORDER BY (order is undefined/insertion-order-ish).
- Also searches `facility_subtype`, not `address`.

### Program keyword search — `ProgramRepositoryCustomImpl` (native `EntityManager.createNativeQuery`, QueryDSL is NOT used for search despite being on the classpath)
File: `src/main/java/org/wemightmove/movemap/domain/program/repository/ProgramRepositoryCustomImpl.java:491-542`
```java
sql.append("""
    SELECT p.id, p.name as program_name, p.facility_name as facility_name,
           p.facility_subtype, p.address
    FROM program p WHERE 1=1
    """);
if (cursor != null) { sql.append("AND p.id > :cursor "); params.put("cursor", cursor); }
sql.append("""
    AND (
        p.name_normalized ILIKE :keyword
        OR p.facility_name_normalized ILIKE :keyword
    )
    """);
params.put("keyword", keyword + "%");   // prefix only, one-sided
sql.append("ORDER BY p.id ASC LIMIT :size");
```
- **CONFIRMED**: prefix search only (`keyword + "%"`, no leading `%`), against **generated/stored columns** `name_normalized` and `facility_name_normalized`, using `ILIKE`.
- Cursor is `id > :cursor`, sort `id ASC` — matches the "다음 커서" javadoc but is unusual (ascending ID paging rather than relevance paging).
- `keyword` param here is `normalizedKeyword`, produced by:

**Normalization rule — CONFIRMED, exact code:**
File: `src/main/java/org/wemightmove/movemap/domain/program/dto/request/ProgramSearchByKeywordRequest.java:28-31`
```java
// 공백 제거한 정규화된 키워드 반환
public String normalizedKeyword() {
    return keyword.replaceAll("\\s+", "");
}
```
Called at `ProgramQueryServiceImpl.java:127`: `String normalizedKeyword = request.normalizedKeyword();`, then passed into `searchProgramsByKeyword(normalizedKeyword, request.cursor(), size + 1)` (line 132-136).

**`name_normalized` / `facility_name_normalized` — CONFIRMED as DB-generated STORED columns, not app-computed:**
- `V14.1__add_program_name_normalized_column.sql`: installs `pg_trgm` extension, then
  ```sql
  ALTER TABLE program ADD COLUMN name_normalized VARCHAR(200)
      GENERATED ALWAYS AS (REPLACE(name, ' ', '')) STORED;
  ```
  (only strips literal space char, not all whitespace/tabs — slightly narrower than Java's `\s+`)
- `V14.2__add_program_name_normalized_index.sql`: `CREATE INDEX idx_program_name_normalized_btree ON program(name_normalized);` — **plain B-Tree**, comment says "일반 방식" (regular method) and warns "개발 환경에서만 사용 권장" (dev-only recommended).
- `V14.4__add_facility_name_normalized_program_tables.sql`: same pattern for `facility_name_normalized` (`REPLACE(COALESCE(facility_name,''), ' ', '')`) + `idx_program_facility_name_normalized_btree`.
- These generated columns are **DB-only** — not mapped on the `Program` JPA entity at all (confirmed: `Program.java` has no `nameNormalized` field); only reachable via native SQL.

**UNCONFIRMED / flag for downstream**: `pg_trgm` extension is installed (V14.1) but the index actually created is a **plain B-Tree**, not a GIN trgm index. A plain B-Tree generally cannot be used by the planner for `ILIKE 'prefix%'` unless the DB collation is `C` (case-insensitive prefix matching normally defeats a default-collation B-Tree). Whether this index is actually used by the planner for the `ILIKE :keyword` (keyword+`%`) query was **not verified via EXPLAIN in this pass** — worth checking against the `perf/out/explain_*` artifacts already in this worktree (see §9) before concluding it's dead weight.

### Other native-SQL search-adjacent queries (list/marker endpoints, for context — not the `/search` endpoints)
- `FacilityRepositoryCustomImpl.findMarkersByViewport` / `findListByViewport`: `f.name ILIKE :keyword` with `%keyword%` (both-sided) — file `.../facility/repository/FacilityRepositoryCustomImpl.java:114`, `:306`.
- `ProgramRepositoryCustomImpl.findMarkersByViewport` / `findProgramsByViewport`: `p.name ILIKE :keyword` (viewport marker, line 152) and `(p.name ILIKE :keyword OR p.address ILIKE :keyword OR p.facility_subtype ILIKE :keyword)` (viewport list, line 441) — both-sided `%keyword%`, against raw (non-normalized) columns, unrelated to the dedicated `/search` autocomplete endpoints.

---

## 3. Entities

### `Facility` — `src/main/java/org/wemightmove/movemap/domain/facility/entity/Facility.java`
Fields (all mapped, DB column names in parens): `id`, `facilityType` (enum, `facility_type`), `facilitySubtype` (`facility_subtype`), `name`, `location` (JTS `Point`, PostGIS `geometry(Point,4326)`), `latitude`/`longitude` (`BigDecimal`, duplicated alongside `location`), `regionCode` (`region_cd`), `address`, `hmpgUrl`, `isVoucherAvailable`.
- No `avgRating`/`reviewCount`/`bookmark` fields on the entity — these are always computed via `LEFT JOIN facility_review` + `AVG/COUNT` and `LEFT JOIN member_facility` + `CASE WHEN ... IS NOT NULL` in native SQL at query time (see `FacilityRepositoryCustomImpl.findListByRegionCode`/`findListByViewport`, `FacilityRepository.findFacilityInfoWithLocation`). Never denormalized/stored.

### `Program` — `src/main/java/org/wemightmove/movemap/domain/program/entity/Program.java`
Fields: `id`, `facilityType`, `facilitySubtype`, `facilityName` (`facility_name`, distinct from program `name`), `name`, `location` (Point), `latitude`/`longitude`, `regionCode`, `address`, `hmpgUrl`, `beginDate`/`endDate`, `weekdayNumber` (`weekday_number`, Integer bitmask 0-127), `price` (Integer), `startTime`/`endTime`, `target` (Integer bitmask 0-16383, age groups), `capacity`.
- Same pattern: `avgRating`/`reviewCount`/`isBookmarked` are always computed via joins at query time, never stored on the entity.
- `name_normalized` / `facility_name_normalized` are NOT entity fields (DB-generated-column-only, see §2).

### Bitmask decode/encode utilities (both exist under `global/util`, dead-simple bit ops, well unit-tested)
- `WeekdayUtil` (`src/main/java/org/wemightmove/movemap/global/util/WeekdayUtil.java`): 7-bit mask, bit0=Mon...bit6=Sun (`MONDAY_BIT=1 ... SUNDAY_BIT=64`), `ALL_DAYS_MASK=127`. Key methods: `decodeWeekdays(Integer)→String[]`, `decodeWeekdaysSimplified(Integer)→String` (collapses to "평일"/"주말"/"매일"), `encodeWeekdayString(String)→int`.
- `AgeGroupUtil` (`.../global/util/AgeGroupUtil.java`): 14-bit mask, bit0-5=초등1-6학년, bit6-8=중1-3, bit9-11=고1-3, bit12=성인, bit13=시니어; `ALL_AGES_MASK=16383`. `decodeAgeGroups`, `decodeAgeGroupsSimplified`, `encodeAgeGroups`.
- `ProgramBitmaskUtil` (`.../global/util/ProgramBitmaskUtil.java`): a **second, overlapping** implementation used specifically by search/filter query building (`weekdaysToBitmask(List<Integer>)`, `ageToBitmask(Integer min, Integer max)`) — imported statically in `ProgramRepositoryCustomImpl` (`import static ...ProgramBitmaskUtil.ageToBitmask/weekdaysToBitmask`). Bit layout is defined independently here (same numeric values as WeekdayUtil/AgeGroupUtil but re-declared, not shared constants) — a latent consistency risk if one is changed without the other.
- All three have dedicated unit tests: `AgeGroupUtilTest`, `WeekdayUtilTest`, `ProgramBitmaskUtilTest` under `src/test/java/org/wemightmove/movemap/global/util/`.

---

## 4. Write paths (outbox hook points)

**Key finding**: There is **no application-level create/update/delete path for `Facility` or `Program` entities**. Searched for `facilityRepository.save`, `programRepository.save`, `new Facility(`, `new Program(`, builder patterns — none found outside entities/tests. The only mutation touching these aggregates from the service layer is **bookmark toggling** (a join-table row, not the Facility/Program row itself):

- `FacilityCommandServiceImpl` (`src/main/java/org/wemightmove/movemap/domain/facility/service/FacilityCommandServiceImpl.java`)
  - `addBookmarkFacility(memberId, facilityId)` — `@Transactional` (line 28) — reads Facility/Member, saves `MemberFacility`.
  - `deleteBookmarkFacility(memberId, facilityId)` — `@Transactional` (line 41) — deletes `MemberFacility` row.
- `ProgramCommandServiceImpl` (`src/main/java/org/wemightmove/movemap/domain/program/service/ProgramCommandServiceImpl.java`)
  - `addBookmarkProgram` / `deleteBookmarkProgram` — same shape, `@Transactional` at lines 24 and 37.
- `FacilityReviewServiceImpl` / `ProgramReviewCommandServiceImpl` exist (write `FacilityReview`/`ProgramReview` rows) but these affect `avgRating`/`reviewCount` which are **computed at query time via JOIN+AVG**, not stored on Facility/Program — so a review write does not by itself require a search-index update unless the ES design chooses to denormalize rating/count into the document.

**Implication for an outbox design**: `facility`/`program` base rows are populated exclusively via Flyway migration scripts (`V3.*__insert_facility_data.sql`, `V4.*`/`V5.*__insert_program_data.sql`, and the `V14.3`/`V14.4` UPDATE-heavy migrations) — i.e., bulk/batch data loading, not a live CRUD write path from the running application. There is currently **no natural transactional-outbox hook point** in application code for facility/program row changes; if new facilities/programs are ever added outside a migration, that mechanism does not exist yet in this codebase. Bookmark and review writes are the only live `@Transactional` mutations touching these domains, and they don't change the base row's indexable fields.

---

## 5. Persistence stack (build.gradle)

File: `build.gradle`
- **Java 17** (`JavaLanguageVersion.of(17)`, line 17) — the coordinating agent's brief said "Java 21"; **this is a discrepancy — CONFIRMED the actual toolchain is 17, not 21.**
- **Spring Boot 3.5.7** (line 3), dependency-management plugin 1.1.7.
- Spring Data JPA: `spring-boot-starter-data-jpa` (line 32).
- QueryDSL: `com.querydsl:querydsl-jpa:5.0.0:jakarta` + APT annotation processor (lines 93-96) — present on classpath, Q-classes generated to `build/generated/querydsl`, but **not actually used for the search queries** (both Facility and Program custom repos use raw `EntityManager.createNativeQuery` with hand-built SQL strings, not QueryDSL's fluent API) — CONFIRMED by reading both `*RepositoryCustomImpl` files.
- Flyway: `org.flywaydb:flyway-database-postgresql` (line 76). Migrations at `src/main/resources/db/migration/`, versions range `V1` to `V17` with many `Vx.y` sub-versions (data-insert scripts); latest schema-affecting migrations are `V14.1`-`V14.5`, `V15`, `V16`, `V17` (video table, league status).
- Testcontainers: `org.testcontainers:junit-jupiter:1.20.1` + `com.redis.testcontainers:testcontainers-redis:1.6.4` are **declared** (lines 59-60) but **not actually used anywhere** — no `@Container`/`Testcontainers` usage found in `src/test` (see §7).
- Redis: `spring-boot-starter-data-redis`, included as active profile (`spring.profiles.include: redis` in `application.yml`).
- PostgreSQL + PostGIS: `runtimeOnly 'org.postgresql:postgresql'`, `org.hibernate:hibernate-spatial:6.3.1.Final` + `org.hibernate.orm:hibernate-spatial` (lines 51-53).
- **No Spring Data Elasticsearch / Elasticsearch Java client / OpenSearch dependency anywhere** — CONFIRMED via grep across `src/main`, `build.gradle` (case-insensitive `elastic`/`opensearch`) with zero hits.
- Other notables: JWT (`jjwt` 0.11.5), Firebase Admin SDK 9.2.0, `spring-boot-starter-aop` + `spring-retry` (notification retry), Thymeleaf (mail templates), Bean Validation.

---

## 6. Config & security

- **Profiles**: `application.yml` (checked in) only sets `spring.profiles.active: develop` and `spring.profiles.include: redis`. The actual `application-develop.yml`, `application-deploy.yml`, `application-redis.yml`, `application-loadtest.yml`, `application-firebase.yml` are **gitignored** (`.gitignore` lines under "### yml, test code ###": `src/main/resources/application-develop.yml`, `application-deploy.yml`, `application-redis.yml`, `application-loadtest.yml`, `test_data/`, the firebase service-account JSON, `application-firebase.yml`). So DB credentials/URLs are not in the repo.
- This worktree adds `src/main/resources/application-perf.yml` (currently untracked/new, per git status) — a self-contained "perf" profile with dummy secrets specifically so the app can boot without the gitignored `develop` secrets, wired to `jdbc:postgresql://localhost:5432/movemap`, Flyway enabled (`baseline-on-migrate: true`), Redis at `localhost:6379`. Comment block at top explains its purpose (measurement-only, not real deploy config). This is part of an existing performance-baseline effort in this same worktree (`perf/` directory with prior benchmark run outputs — see §9).
- **SecurityConfig**: `src/main/java/org/wemightmove/movemap/global/config/SecurityConfig.java`
  - `AUTH_WHITELIST` (lines 35-39): `/error`, `/favicon.ico`, `/health`, `/swagger-ui/**`, `/swagger-ui.html`, `/v3/api-docs`, `/v3/api-docs/**`, `/auth/signup`, `/auth/login`, `/auth/kakao`, `/auth/token`, `/auth/email/*`.
  - Everything else (`.anyRequest().authenticated()`, line 71) requires JWT — **`/facilities/search` and `/programs/search` are NOT whitelisted, both require a valid JWT.**
  - Stateless session (`SessionCreationPolicy.STATELESS`), CSRF disabled, custom `JwtAuthenticationFilter` inserted before `LogoutFilter`, `ExceptionHandlerFilter` before the JWT filter.
  - CORS configured from `CorsConfigProperties` (externalized, allowed origins/methods configurable).
- **No Elasticsearch/OpenSearch config anywhere** (no `application-elasticsearch.yml`, no ES client bean, no `spring.elasticsearch.*` properties) — CONFIRMED absent.

---

## 7. Existing tests

`src/test/java/org/wemightmove/movemap/` contains exactly 4 files:
- `MovemapApplicationTests.java` — bare `@SpringBootTest` context-load smoke test, no assertions, no Testcontainers/DB setup wired in (would need a real Postgres reachable, or would fail — profile used at test time not evident from this file alone).
- `global/util/AgeGroupUtilTest.java`, `global/util/WeekdayUtilTest.java`, `global/util/ProgramBitmaskUtilTest.java` — pure unit tests of the bitmask utilities in §3.

**CONFIRMED absent**: no repository tests, no controller/`@WebMvcTest` tests, no integration tests for any `/search`, `/list`, or `/markers` endpoint, no test exercising `searchFacilitiesByNameAndFacilitySubtype` or `searchProgramsByKeyword` directly, and despite Testcontainers being a declared dependency (§5), it is not invoked anywhere in `src/test`. This is a green field for a downstream planner to add ES parity/regression tests — there's no existing search test suite to keep backward-compatible.

---

## 8. Package structure (convention for where ES code should live)

Top-level: `org.wemightmove.movemap` under `src/main/java/org/wemightmove/movemap/`
- `domain/{auth,facility,league,member,notification,program,record,video}/` — each domain follows:
  - `controller/`
  - `dto/` (with `dto/request/`, `dto/response/` subpackages; loose projection DTOs like `FacilityInfoProjection.java` sit directly under `dto/`)
  - `entity/`
  - `repository/` (JPA repo interface extends `JpaRepository<T,ID>` + a `*RepositoryCustom` interface; implementation is `*RepositoryCustomImpl` in the same package, `@Repository`-annotated, using raw `EntityManager` native queries)
  - `service/` (interface + `*ServiceImpl`, split further into `*QueryService`/`*CommandService` per domain — e.g. `FacilityQueryService`/`FacilityCommandService`/`FacilityReviewService`, `ProgramQueryService`/`ProgramCommandService`/`ProgramReviewCommandService`/`ProgramReviewQueryService`)
- `global/` — cross-cutting: `client/` (+`client/dto`), `config/`, `entity/` (e.g. shared `RegionType`), `enums/` (e.g. `FacilityType`, `WeekDayType`), `exception/` (`CustomException`, `ErrorCode`), `jwt/`, `repository/` (shared repos like `RegionTypeRepository`), `security/`, `util/` (bitmask/weekday utils).

**Convention implication for ES code placement**: an ES-backed search implementation would naturally live as a new `*RepositoryCustomImpl`-style adapter or a new `service`-layer implementation within `domain/facility/` and `domain/program/` respectively (keeping the existing `*QueryService` interface contracts stable), plus possibly a `global/config/` bean for the ES client/`RestHighLevelClient`/`ElasticsearchOperations`, following the existing `*Config` class pattern seen in `global/config/SecurityConfig.java`. There is no existing `search/` or `infrastructure/` package — this repo is strictly domain-first with `controller/dto/entity/repository/service` per domain, no hexagonal/ports-adapters layering currently.

---

## 9. Adjacent context found in this worktree (not requested, but relevant)

This worktree (`perf-baseline-p1`) already contains a `perf/` directory with prior baseline benchmark artifacts: `perf/README.md`, `perf/BENCHMARK_REPORT.md`, `perf/AS-IS-BASELINE-REPORT.md`, `perf/docker-compose.yml`, and `perf/out/*` (EXPLAIN plans, pgstat captures, JSON summaries, HTML reports) for both `facilities` and `programs` search paths under baseline/openmodel/A/B scenarios. `perf/out/explain_baseline_facilities.txt`, `explain_baseline_programs.txt`, `explain_A_facilities.txt`, `explain_A_programs.txt`, `explain_B_programs.txt` are likely the fastest way to settle the "is the B-Tree index on `name_normalized` actually used by the planner" question flagged as UNCONFIRMED in §2 — these were not opened in this pass since they were out of scope for the codebase-facts request, but a downstream planning agent should read them before designing ES field/index equivalents.
