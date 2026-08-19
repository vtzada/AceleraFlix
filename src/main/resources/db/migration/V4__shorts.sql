ALTER TABLE video
    ADD COLUMN duracao_segundos INTEGER,
    ADD COLUMN es_short BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX idx_video_es_short ON video (es_short);