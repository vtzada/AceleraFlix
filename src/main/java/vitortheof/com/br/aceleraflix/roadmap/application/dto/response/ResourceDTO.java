package vitortheof.com.br.aceleraflix.roadmap.application.dto.response;

import vitortheof.com.br.aceleraflix.roadmap.domain.ResourceType;

import java.util.UUID;

public record ResourceDTO(
        UUID id,
        String title,
        ResourceType type,
        String externalUrl,
        UUID videoId
) {
}
