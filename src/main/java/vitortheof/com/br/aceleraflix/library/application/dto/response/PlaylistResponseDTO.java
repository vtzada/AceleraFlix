package vitortheof.com.br.aceleraflix.library.application.dto.response;

import vitortheof.com.br.aceleraflix.library.domain.PlaylistType;

import java.util.List;
import java.util.UUID;

public record PlaylistResponseDTO(
        UUID id,
        String title,
        PlaylistType type,
        List<PlaylistVideoResponse> videos
) {
}
