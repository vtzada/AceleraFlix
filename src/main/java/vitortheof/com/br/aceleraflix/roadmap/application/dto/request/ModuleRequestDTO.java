package vitortheof.com.br.aceleraflix.roadmap.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ModuleRequestDTO(
        @NotBlank(message = "O título é obrigatório")
        String title,
        @NotNull(message = "A ordem é obrigatória")
        Integer orderIndex
) {
}
