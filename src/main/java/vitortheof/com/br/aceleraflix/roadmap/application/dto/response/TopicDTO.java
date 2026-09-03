package vitortheof.com.br.aceleraflix.roadmap.application.dto.response;

import java.util.List;
import java.util.UUID;

public record TopicDTO(
        UUID id,
        String title,
        List<ResourceDTO> resources
) {
}
