INSERT INTO league_status (region_id, type, created_at, updated_at)
SELECT id, 'START', NOW(), NOW()
FROM region_type
WHERE parent_id IS NOT NULL;