package vitortheof.com.br.aceleraflix.video.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record VideoUpdateRequest(
        @NotBlank(message = "Título é obrigatório")
        @Size(max = 160, message = "Título é obrigatório deve ter no máx. 160 caracteres.")
        String titulo,
        @Size(max = 2000, message = "Descrição deve ter no máx. 2000 caracteres.")
        String descricao,
        UUID categoriaId,
        List<String> tags
) {
}