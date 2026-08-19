package vitortheof.com.br.aceleraflix.video.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record VideoRequest(
        @NotBlank(message = "TA-tulo Ac obrigatA3rio")
        @Size(max = 160, message = "TA-tulo deve ter no mA�ximo 160 caracteres")
        String titulo,
        @Size(max = 2000, message = "DescriAA�o deve ter no mA�ximo 2000 caracteres")
        String descricao,
        @NotBlank(message = "URL do youtube Ac obrigatA3ria")
        String urlYoutube,
        UUID categoriaId,
        List<String> tags
) {
}