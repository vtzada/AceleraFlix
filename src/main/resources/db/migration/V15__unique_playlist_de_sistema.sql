-- Cada usuário deve ter no máximo uma playlist de sistema por tipo (WATCH_LATER, LIKED).
-- Antes de criar o índice único, funde as duplicadas existentes: fica a de menor id
-- e os vídeos das demais são movidos para ela.

CREATE TEMPORARY TABLE playlist_duplicada AS
SELECT id AS duplicada_id, manter_id
FROM (SELECT id,
             first_value(id) OVER (PARTITION BY user_id, type ORDER BY id) AS manter_id
      FROM playlist
      WHERE type IN ('WATCH_LATER', 'LIKED')) ranqueada
WHERE id <> manter_id;

-- Um mesmo vídeo pode estar em várias duplicadas (ou já na que fica): entra uma única vez,
-- respeitando uk_playlist_video e preservando a data mais antiga entre as duplicadas.
INSERT INTO playlist_videos (id, playlist_id, video_id, added_at)
SELECT gen_random_uuid(), d.manter_id, pv.video_id, MIN(pv.added_at)
FROM playlist_videos pv
         JOIN playlist_duplicada d ON d.duplicada_id = pv.playlist_id
GROUP BY d.manter_id, pv.video_id
ON CONFLICT (playlist_id, video_id) DO NOTHING;

-- ON DELETE CASCADE remove os playlist_videos das duplicadas (já copiados acima).
DELETE FROM playlist
WHERE id IN (SELECT duplicada_id FROM playlist_duplicada);

DROP TABLE playlist_duplicada;

CREATE UNIQUE INDEX uk_playlist_user_tipo_sistema
    ON playlist (user_id, type)
    WHERE type IN ('WATCH_LATER', 'LIKED');
