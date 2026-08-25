package vitortheof.com.br.aceleraflix.profile.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record PerfilRequest(
        @NotBlank
        @Size(min = 5, max = 20, message = "O mínimo de caracteres é 5, e o máximo são 20 caracteres.")
        @Pattern(regexp = "^[a-z0-9_]+$")
        String username,
        @Size(max = 255, message = "O limite permitido de caracteres é 255.")
        String bio,
        @Pattern(regexp = "^(https://github\\.com/.*)?$", message = "Deve ser uma URL válida do GitHub com HTTPS")
        String githubUrl,
        @Pattern(regexp = "^(https://(www\\.)?linkedin\\.com/.*)?$", message = "Deve ser uma URL válida do LinkedIn com HTTPS")
        String linkedinUrl,
        @URL(message = "O avatar deve ser uma URL válida")
        String avatarUrl) {
}
