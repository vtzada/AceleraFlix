package vitortheof.com.br.aceleraflix.video.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record VideoUpdateRequest(
        @NotBlank(message = "TA-tulo Ac obrigatA3ria")
        @Size(max = 160, message = "TA-tulo deve ter no mA�x. 160 caracteres.")
        String titulo,
        @Size(max = 2000, message = "DescricA�o deve ter no mA�x. 2000 caracteres.")
        String descricao,
        UUID categoriaId,
        List<String> tags
) {
}