package vitortheof.com.br.aceleraflix.category.application.dto;

import java.util.UUID;

public record CategoriaDTO(UUID id,
                           String nome,
                           String slug) {
}
