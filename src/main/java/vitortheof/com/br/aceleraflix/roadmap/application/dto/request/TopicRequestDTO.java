package vitortheof.com.br.aceleraflix.roadmap.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TopicRequestDTO(
        @NotBlank(message = "O título é obrigatório.")
        String title,
        @NotNull(message = "A ordem é obrigatória.")
        String orderIndex) {
}
