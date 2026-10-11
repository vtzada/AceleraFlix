package vitortheof.com.br.aceleraflix.roadmap.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RoadmapRequestDTO(
        @NotBlank(message = "O título é obrigatório")
        @Size(max = 255, message = "O título deve ter no máximo 255 caracteres")
        String title,

        @NotBlank(message = "O slug é obrigatório")
        @Size(max = 255, message = "O slug deve ter no máximo 255 caracteres")
        @Pattern(regexp = "^[a-z0-9-]+$", message = "O slug deve conter apenas letras minúsculas, números e hífens")
        String slug,

        String description
) {
}
