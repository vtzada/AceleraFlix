package vitortheof.com.br.aceleraflix.video.application.exception;

import java.util.UUID;

public class CategoryCategoryNonExistentException extends RuntimeException {
    public CategoryCategoryNonExistentException(UUID categoriaId) {
        super("Categoria não existe: " + categoriaId);
    }
}
