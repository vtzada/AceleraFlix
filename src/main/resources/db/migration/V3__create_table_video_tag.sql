CREATE TABLE tag
(
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE video
(
    id               UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    titulo           VARCHAR(160) NOT NULL,
    descricao        TEXT,
    url_embed        VARCHAR(255) NOT NULL,
    video_externo_id VARCHAR(30)  NOT NULL,
    plataforma       VARCHAR(20)  NOT NULL DEFAULT 'YOUTUBE',
    thumbnail_url    VARCHAR(255),
    categoria_id     UUID         NOT NULL REFERENCES categoria (id),
    criado_por       UUID         NOT NULL REFERENCES usuario (id),
    status           VARCHAR(20)  NOT NULL DEFAULT 'APROVADO',
    criado_em        TIMESTAMP    NOT NULL DEFAULT now(),
    atualizado_em    TIMESTAMP
);

CREATE TABLE video_tag
(
    video_id UUID NOT NULL REFERENCES video (id) ON DELETE CASCADE,
    tag_id   UUID NOT NULL REFERENCES tag (id) ON DELETE CASCADE,
    PRIMARY KEY (video_id, tag_id)
);

CREATE INDEX idx_video_categoria ON video (categoria_id);
CREATE INDEX idx_video_status ON video (status);
CREATE INDEX idx_video_criado_por ON video (criado_por);