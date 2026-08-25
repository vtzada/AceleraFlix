package vitortheof.com.br.aceleraflix.video.application.dto;

import java.util.UUID;

public record CriadorDTO(
        UUID id,
        String nome,
        String username,
        String avatarUrl) {
}
