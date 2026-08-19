package vitortheof.com.br.aceleraflix.video.infrastructure.youtube;

import java.util.List;

public record YoutubeChannelResponse(List<ChannelItem> items) {

    public record ChannelItem(String id, ContentDetails contentDetails) {}

    public record ContentDetails(RelatedPlaylists relatedPlaylists) {}

    public record RelatedPlaylists(String uploads) {}

}