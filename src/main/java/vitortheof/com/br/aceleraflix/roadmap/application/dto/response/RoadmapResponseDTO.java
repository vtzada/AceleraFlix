package vitortheof.com.br.aceleraflix.roadmap.application.dto.response;

import java.util.List;
import java.util.UUID;

public record RoadmapResponseDTO(
        UUID id,
        String title,
        String slug,
        String description,
        List<ModuleDTO> modules
) {
}
