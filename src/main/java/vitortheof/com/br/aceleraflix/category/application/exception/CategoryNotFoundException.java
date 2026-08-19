package vitortheof.com.br.aceleraflix.category.application.exception;

import java.util.UUID;

public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException(UUID id) {
        super("Categoria não encontrado: " + id);
    }
}
