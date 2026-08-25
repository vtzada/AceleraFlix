-- Rollback da feature de trilhas (V8/V9).
-- Usa IF EXISTS para ser segura tanto se V8/V9 já foram aplicadas quanto se não foram.
DROP TABLE IF EXISTS progresso_topico;
DROP TABLE IF EXISTS topico_trilha;
DROP TABLE IF EXISTS trilha;

-- As tags semeadas em V9 (ids c0000000-...) são mantidas de propósito: são tags
-- genéricas úteis e podem estar referenciadas por vídeos reais via video_tag.
