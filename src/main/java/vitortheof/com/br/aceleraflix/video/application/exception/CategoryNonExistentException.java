package vitortheof.com.br.aceleraflix.video.application.exception;

import java.util.UUID;

public class CategoryNonExistentException extends RuntimeException {
    public CategoryNonExistentException(UUID categoriaId) {
        super("Categoria não existe: " + categoriaId);
    }
}
