package vitortheof.com.br.aceleraflix.video.infrastructure.youtube;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.List;
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

    public Optional<String> buscarUploadsPlaylistPorHandle(String handle) {
        try {
            YoutubeChannelResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/channels")
                            .queryParam("part", "contentDetails")
                            .queryParam("forHandle", handle)
                            .queryParam("key", apiKey)
                            .build())
                    .retrieve()
                    .body(YoutubeChannelResponse.class);

            if (response == null || response.items() == null || response.items().isEmpty()) {
                return Optional.empty();
            }
            String uploads = response.items().get(0).contentDetails() != null
                    && response.items().get(0).contentDetails().relatedPlaylists() != null
                    ? response.items().get(0).contentDetails().relatedPlaylists().uploads()
                    : null;
            return Optional.ofNullable(uploads);
        } catch (RestClientException ex) {
            throw new YoutubeApiException("Erro ao consultar a API do YouTube: " + ex.getMessage());
        }
    }

    public List<YoutubeVideoDetalhes> buscarUploadsRecentes(String playlistId, int maxResultados) {
        try {
            YoutubePlaylistResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/playlistItems")
                            .queryParam("part", "snippet")
                            .queryParam("playlistId", playlistId)
                            .queryParam("maxResults", Math.min(maxResultados, 50))
                            .queryParam("key", apiKey)
                            .build())
                    .retrieve()
                    .body(YoutubePlaylistResponse.class);

            if (response == null || response.items() == null || response.items().isEmpty()) {
                return List.of();
            }

            List<String> videoIds = response.items().stream()
                    .map(YoutubePlaylistResponse.PlaylistItem::snippet)
                    .map(YoutubePlaylistResponse.Snippet::resourceId)
                    .map(YoutubePlaylistResponse.ResourceId::videoId)
                    .filter(id -> id != null && !id.isBlank())
                    .toList();

            if (videoIds.isEmpty()) {
                return List.of();
            }

            return buscarVideosPorIds(videoIds);
        } catch (RestClientException ex) {
            throw new YoutubeApiException("Erro ao consultar a API do YouTube: " + ex.getMessage());
        }
    }

    public List<YoutubeVideoDetalhes> buscarVideosPorIds(List<String> videoIds) {
        try {
            List<String> lote = new ArrayList<>(videoIds);
            List<YoutubeVideoDetalhes> detalhes = new ArrayList<>();

            for (int i = 0; i < lote.size(); i += 50) {
                List<String> ids = lote.subList(i, Math.min(i + 50, lote.size()));

                YoutubeApiResponse response = restClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/videos")
                                .queryParam("part", "snippet,contentDetails")
                                .queryParam("id", String.join(",", ids))
                                .queryParam("key", apiKey)
                                .build())
                        .retrieve()
                        .body(YoutubeApiResponse.class);

                if (response == null || response.items() == null) {
                    continue;
                }

                for (YoutubeApiResponse.Item item : response.items()) {
                    YoutubeApiResponse.Snippet snippet = item.snippet();
                    String thumbnail = snippet.thumbnails().high().url() != null
                            ? snippet.thumbnails().high().url()
                            : snippet.thumbnails().padrao().url();
                    Integer duracaoSegundos = item.contentDetails() != null
                            ? YoutubeDurationParser.paraSegundos(item.contentDetails().duration())
                            : null;
                    detalhes.add(new YoutubeVideoDetalhes(
                            item.id(),
                            snippet.title(),
                            thumbnail,
                            duracaoSegundos));
                }
            }

            return detalhes;
        } catch (RestClientException ex) {
            throw new YoutubeApiException("Erro ao consultar a API do YouTube: " + ex.getMessage());
        }
    }
}