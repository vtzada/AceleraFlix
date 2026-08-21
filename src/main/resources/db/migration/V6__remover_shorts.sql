DELETE FROM video WHERE categoria_id IS NULL;

DROP INDEX IF EXISTS idx_video_es_short;

ALTER TABLE video DROP COLUMN IF EXISTS es_short;

ALTER TABLE video ALTER COLUMN categoria_id SET NOT NULL;