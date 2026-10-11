package vitortheof.com.br.aceleraflix.roadmap.application.dto.response;

import vitortheof.com.br.aceleraflix.roadmap.domain.ResourceType;

import java.util.UUID;

public record ResourceDTO(
        UUID id,
        String title,
        Integer orderIndex,
        ResourceType type,
        String externalUrl,
        UUID videoId
) {
}
