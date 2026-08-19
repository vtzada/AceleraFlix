package vitortheof.com.br.aceleraflix.video.infrastructure.youtube;

public record YoutubeVideoDetalhes(
        String videoId,
        String titulo,
        String thumbnailUrl,
        Integer duracaoSegundos) {
}