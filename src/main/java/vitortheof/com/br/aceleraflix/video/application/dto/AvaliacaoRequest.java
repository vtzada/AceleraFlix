package vitortheof.com.br.aceleraflix.video.application.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

// BigDecimal (e não Integer) para que nota fracionada como 4.5 seja rejeitada com 400
// em vez de truncada silenciosamente pelo Jackson.
public record AvaliacaoRequest(
        @NotNull(message = "Nota é obrigatória")
        @Min(value = 1, message = "Nota deve ser um inteiro entre 1 e 5")
        @Max(value = 5, message = "Nota deve ser um inteiro entre 1 e 5")
        @Digits(integer = 1, fraction = 0, message = "Nota deve ser um inteiro entre 1 e 5")
        BigDecimal nota) {
}
