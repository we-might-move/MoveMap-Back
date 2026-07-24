-- 최초 컨테이너 생성 시 1회 실행됨 (docker-entrypoint-initdb.d)
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS pg_stat_statements;
CREATE EXTENSION IF NOT EXISTS pg_trgm;
