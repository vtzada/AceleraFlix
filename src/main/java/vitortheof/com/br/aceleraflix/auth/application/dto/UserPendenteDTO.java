package vitortheof.com.br.aceleraflix.auth.application.dto;

import java.time.Instant;
import java.util.UUID;

public record UserPendenteDTO(
        UUID id,
        String nome,
        String email,
        Instant criadoEm
) {
}
