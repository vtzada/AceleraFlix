CREATE TABLE playlist
(
    id      UUID PRIMARY KEY,
    user_id UUID         NOT NULL,
    title   VARCHAR(100) NOT NULL,
    type    VARCHAR(20)  NOT NULL
);

CREATE INDEX idx_playlist_user_id ON playlist (user_id);

CREATE TABLE playlist_videos
(
    id          UUID PRIMARY KEY,
    playlist_id UUID NOT NULL,
    video_id    UUID NOT NULL,
    added_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_playlist_videos_playlist FOREIGN KEY (playlist_id) REFERENCES playlist (id) ON DELETE CASCADE,
    CONSTRAINT uk_playlist_video UNIQUE (playlist_id, video_id)
);