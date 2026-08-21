package vitortheof.com.br.aceleraflix.shared.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ErroResponse(
        String erro,
        int status,
        LocalDateTime timestamp,
        List<String> detalhes) {

    public ErroResponse(String erro, int status) {
        this(erro, status, LocalDateTime.now(), null);
    }
}

