CREATE TABLE usuario
(
    id            UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    nome          VARCHAR(120) NOT NULL,
    email         VARCHAR(180) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL DEFAULT 'USUARIO',
    status_editor VARCHAR(20)  NOT NULL DEFAULT 'NENHUM',
    criado_em     TIMESTAMP    NOT NULL DEFAULT now(),
    atualizado_em TIMESTAMP
);

CREATE INDEX idx_usuario_email ON usuario (email);
CREATE INDEX idx_usuario_status_editor ON usuario (status_editor);