package vitortheof.com.br.aceleraflix.auth.infrastructure.security;

public class TokenInvalidoException extends RuntimeException {
    public TokenInvalidoException(String message) {
        super(message);
    }
}
