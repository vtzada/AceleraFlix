package vitortheof.com.br.aceleraflix.library.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vitortheof.com.br.aceleraflix.auth.infrastructure.security.UserAuth;
import vitortheof.com.br.aceleraflix.library.application.PlaylistService;
import vitortheof.com.br.aceleraflix.library.application.dto.response.PlaylistResponseDTO;
import vitortheof.com.br.aceleraflix.library.application.mapper.PlaylistMapper;
import vitortheof.com.br.aceleraflix.library.repository.PlaylistRepository;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/playlists")
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistService playlistService;
    private final PlaylistRepository playlistRepository;

    @GetMapping
    public ResponseEntity<List<PlaylistResponseDTO>> getUserPlaylists(@AuthenticationPrincipal UserAuth userAuth ) {

        UUID userId = userAuth.getUsuario().getId();

        List<PlaylistResponseDTO> playlists = playlistRepository.findAllByUserId(userId)
                .stream()
                .map(PlaylistMapper::toDTO)
                .toList();

        return ResponseEntity.ok(playlists);
    }

    @PostMapping("/watch-later/{videoId}")
    public ResponseEntity<Void> toggleWatchLater(@AuthenticationPrincipal UserAuth userAuth, @PathVariable UUID videoId) {
        UUID userId = userAuth.getUsuario().getId();
        playlistService.toggleWatchLater(userId, videoId);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/liked/{videoId}")
    public ResponseEntity<Void> toggleLikedVideo(@AuthenticationPrincipal UserAuth userAuth, @PathVariable UUID videoId) {
        UUID userId = userAuth.getUsuario().getId();
        playlistService.toggleLikedVideo(userId, videoId);
        return ResponseEntity.ok().build();
    }

}
