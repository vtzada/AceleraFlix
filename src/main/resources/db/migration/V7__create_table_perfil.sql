CREATE TABLE perfil
(
    id           UUID PRIMARY KEY,
    usuario_id   UUID        NOT NULL UNIQUE,
    username     VARCHAR(25) NOT NULL UNIQUE,
    bio          VARCHAR(255),
    avatar_url   VARCHAR(500),
    github_url   VARCHAR(255),
    linkedin_url VARCHAR(255),
    verificado   BOOLEAN     NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_perfil_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE CASCADE
);