package vitortheof.com.br.aceleraflix.auth.application.exception;

public class CredentialsInvalidException extends RuntimeException {
    public CredentialsInvalidException() {
        super("Email ou senha inválidos");
    }
}
