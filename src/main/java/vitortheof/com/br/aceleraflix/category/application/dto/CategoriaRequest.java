package vitortheof.com.br.aceleraflix.category.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaRequest(
        @NotBlank(message = "Nome é obrigatório")
        @Size(min = 2, max = 80, message = "Nome deve entre 2 e 80 caracteres")
        String nome,
        @Size(max = 1000, message = "Descricão deve ter no máximo 1000 caracteres")
        String descricao) {
}
