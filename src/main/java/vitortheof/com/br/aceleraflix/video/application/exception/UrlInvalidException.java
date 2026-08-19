package vitortheof.com.br.aceleraflix.video.application.exception;

public class UrlInvalidException extends RuntimeException {
    public UrlInvalidException(String message) {
        super(message);
    }
}
