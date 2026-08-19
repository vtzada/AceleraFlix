package vitortheof.com.br.aceleraflix.video.application.exception;

public class CategoriaObrigatoriaException extends RuntimeException {
    public CategoriaObrigatoriaException() {
        super("Categoria é obrigatória para vídeos que não são Shorts");
    }
}