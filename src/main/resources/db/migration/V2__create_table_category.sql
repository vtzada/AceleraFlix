CREATE TABLE categoria
(
    id         UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    nome       VARCHAR(80) NOT NULL,
    slug       VARCHAR(80) NOT NULL UNIQUE,
    descricao  TEXT,
    criado_por UUID        NOT NULL REFERENCES usuario (id),
    criado_em  TIMESTAMP   NOT NULL DEFAULT now()
);

CREATE INDEX idx_categoria_slug ON categoria (slug);