package vitortheof.com.br.aceleraflix.library.application.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record PlaylistVideoResponse(
        UUID videoId,
        LocalDateTime addedAt) {
}
