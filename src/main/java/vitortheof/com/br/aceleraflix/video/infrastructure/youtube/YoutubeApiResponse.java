package vitortheof.com.br.aceleraflix.video.infrastructure.youtube;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

//Records para desserializar a resposta da api do ytb, colocando so oq necessitamos
public record YoutubeApiResponse(List<Item> items
) {
    public record Item(Snippet snippet) {}

    public record Snippet(String title, Thumbnails thumbnails) {}

    public record Thumbnails(
            @JsonProperty("default") Thumbnail padrao,
            Thumbnail medium,
            Thumbnail high) {}

    public record Thumbnail(String url) {}

}
