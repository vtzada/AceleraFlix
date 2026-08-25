CREATE TABLE trilha
(
    id        UUID PRIMARY KEY,
    slug      VARCHAR(60)  NOT NULL UNIQUE,
    titulo    VARCHAR(100) NOT NULL,
    descricao VARCHAR(255),
    criado_em TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE topico_trilha
(
    id        UUID PRIMARY KEY,
    trilha_id UUID         NOT NULL,
    titulo    VARCHAR(120) NOT NULL,
    descricao VARCHAR(255),
    fase      INT          NOT NULL,
    ordem     INT          NOT NULL,
    tag_nome  VARCHAR(50),

    CONSTRAINT fk_topico_trilha FOREIGN KEY (trilha_id) REFERENCES trilha (id) ON DELETE CASCADE
);
CREATE INDEX idx_topico_trilha_ordem ON topico_trilha (trilha_id, fase, ordem);

CREATE TABLE progresso_topico
(
    id         UUID PRIMARY KEY,
    usuario_id UUID     NOT NULL,
    topico_id  UUID     NOT NULL,
    criado_em  TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT fk_progresso_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE CASCADE,
    CONSTRAINT fk_progresso_topico FOREIGN KEY (topico_id) REFERENCES topico_trilha (id) ON DELETE CASCADE,
    CONSTRAINT uk_progresso_usuario_topico UNIQUE (usuario_id, topico_id)
);
CREATE INDEX idx_progresso_usuario ON progresso_topico (usuario_id);
