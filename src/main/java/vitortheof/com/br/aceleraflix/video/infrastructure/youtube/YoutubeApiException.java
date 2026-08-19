package vitortheof.com.br.aceleraflix.video.infrastructure.youtube;

public class YoutubeApiException extends RuntimeException {
    public YoutubeApiException(String mensagem) {
        super(mensagem);
    }
}
