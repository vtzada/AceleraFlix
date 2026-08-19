package vitortheof.com.br.aceleraflix.category.application.exception;

public class CategoryWithVideoException extends RuntimeException {
    public CategoryWithVideoException() {
        super("Não é possível excluir: existem vídeos vinculados a esta categoria");
    }
}
