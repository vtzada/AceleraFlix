package vitortheof.com.br.aceleraflix.auth.application.exception;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String email) {
        super("Email já cadastrado" + email);
    }
}
