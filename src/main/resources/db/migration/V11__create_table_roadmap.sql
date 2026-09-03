CREATE TABLE roadmaps
(
    id          UUID PRIMARY KEY,
    title       VARCHAR(255) NOT NULL,
    slug        VARCHAR(255) NOT NULL UNIQUE,
    description TEXT
);

CREATE TABLE roadmap_modules
(
    id          UUID PRIMARY KEY,
    roadmap_id  UUID         NOT NULL REFERENCES roadmaps (id) ON DELETE CASCADE,
    title       VARCHAR(255) NOT NULL,
    order_index INT          NOT NULL
);

CREATE TABLE roadmap_topics
(
    id          UUID PRIMARY KEY,
    module_id   UUID         NOT NULL REFERENCES roadmap_modules (id) ON DELETE CASCADE,
    title       VARCHAR(255) NOT NULL,
    order_index INT          NOT NULL
);

CREATE TABLE roadmap_resources
(
    id           UUID PRIMARY KEY,
    topic_id     UUID         NOT NULL REFERENCES roadmap_topics (id) ON DELETE CASCADE,
    title        VARCHAR(255) NOT NULL,
    type         VARCHAR(50)  NOT NULL,
    external_url VARCHAR(500),
    video_id     UUID,
    order_index  INT          NOT NULL
);