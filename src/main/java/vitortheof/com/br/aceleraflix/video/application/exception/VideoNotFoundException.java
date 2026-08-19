package vitortheof.com.br.aceleraflix.video.application.exception;

import java.util.UUID;

public class VideoNotFoundException extends RuntimeException {
    public VideoNotFoundException(UUID id) {
        super("Video nao encontrado" + id);
    }
}
