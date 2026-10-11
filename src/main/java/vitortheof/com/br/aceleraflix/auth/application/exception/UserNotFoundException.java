package vitortheof.com.br.aceleraflix.auth.application.exception;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(UUID userId) {
        super("Usuário não encontrado: " + userId);
    }
}
