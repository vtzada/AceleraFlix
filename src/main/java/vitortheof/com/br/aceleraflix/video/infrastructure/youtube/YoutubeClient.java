package vitortheof.com.br.aceleraflix.video.infrastructure.youtube;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

@Component
public class YoutubeClient {

    private final RestClient restClient;
    private final String apiKey;

    public YoutubeClient(@Value("${youtube.api.key}") String apiKey) {
        this.apiKey = apiKey;
        this.restClient = RestClient.create("https://www.googleapis.com/youtube/v3");
    }

    public Optional<YoutubeVideoMetadata> buscarMetadata(String videoId) {
        try {
            YoutubeApiResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/videos")
                            .queryParam("part", "snippet,contentDetails")
                            .queryParam("id", videoId)
                            .queryParam("key", apiKey)
                            .build())
                    .retrieve()
                    .body(YoutubeApiResponse.class);

            if (response == null || response.items() == null || response.items().isEmpty()) {
                return Optional.empty();
            }

            YoutubeApiResponse.Snippet snippet = response.items().get(0).snippet();
            String thumbnail = snippet.thumbnails().high().url() != null
                    ? snippet.thumbnails().high().url()
                    : snippet.thumbnails().padrao().url();

            YoutubeApiResponse.ContentDetails contentDetails = response.items().get(0).contentDetails();
            Integer duracaoSegundos = contentDetails != null
                    ? YoutubeDurationParser.paraSegundos(contentDetails.duration())
                    : null;

            return Optional.of(new YoutubeVideoMetadata(snippet.title(), thumbnail, duracaoSegundos));

        } catch (RestClientException ex) {
            throw new YoutubeApiException("Erro ao consultar a API do YouTube: " + ex.getMessage());
        }
    }
}