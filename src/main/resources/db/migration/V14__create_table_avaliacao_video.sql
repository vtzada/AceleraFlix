CREATE TABLE avaliacao_video
(
    id            UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    video_id      UUID      NOT NULL REFERENCES video (id) ON DELETE CASCADE,
    usuario_id    UUID      NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    nota          INTEGER   NOT NULL,
    criado_em     TIMESTAMP NOT NULL DEFAULT now(),
    atualizado_em TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uk_avaliacao_video_usuario_video UNIQUE (usuario_id, video_id),
    CONSTRAINT ck_avaliacao_video_nota CHECK (nota BETWEEN 1 AND 5)
);

CREATE INDEX idx_avaliacao_video_video ON avaliacao_video (video_id);
