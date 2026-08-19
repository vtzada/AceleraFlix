package vitortheof.com.br.aceleraflix.video.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record VideoRequest(
        @NotBlank(message = "Título é obrigatório")
        @Size(max = 160, message = "Título deve ter no máximo 160 caracteres")
        String titulo,
        @Size(max = 2000, message = "Descrição deve ter no máximo 2000 caracteres")
        String descricao,
        @NotBlank(message = "URL do youtube é obrigatória")
        String urlYoutube,
        @NotNull(message = "Categoria é obrigatória")
        UUID categoriaId,
        List<String> tags
) {
}
