package vitortheof.com.br.aceleraflix.roadmap.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import vitortheof.com.br.aceleraflix.roadmap.domain.ResourceType;

import java.util.UUID;

public record ResourceRequestDTO(
        @NotBlank(message = "O título é obrigatório")
        String title,
        @NotNull(message = "O tipo do recurso é obrigatório")
        ResourceType type,
        String externalUrl,
        UUID videoId,
        @NotNull(message = "A ordem é obrigatória")
        Integer orderIndex
) {
}
