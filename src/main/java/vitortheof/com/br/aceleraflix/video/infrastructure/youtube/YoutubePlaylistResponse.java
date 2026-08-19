package vitortheof.com.br.aceleraflix.video.infrastructure.youtube;

import java.util.List;

public record YoutubePlaylistResponse(List<PlaylistItem> items) {

    public record PlaylistItem(Snippet snippet) {}

    public record Snippet(String title, ResourceId resourceId) {}

    public record ResourceId(String videoId) {}

}