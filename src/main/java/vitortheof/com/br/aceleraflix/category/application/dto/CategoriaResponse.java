package vitortheof.com.br.aceleraflix.category.application.dto;

import java.time.Instant;
import java.util.UUID;

public record CategoriaResponse(
        UUID id,
        String nome,
        String slug,
        String descricao,
        Instant criadoEm) {
}
