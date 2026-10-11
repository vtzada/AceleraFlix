package vitortheof.com.br.aceleraflix.engagement.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vitortheof.com.br.aceleraflix.engagement.domain.Playlist;
import vitortheof.com.br.aceleraflix.engagement.domain.PlaylistType;
import vitortheof.com.br.aceleraflix.engagement.domain.PlaylistVideo;
import vitortheof.com.br.aceleraflix.engagement.infrastructure.PlaylistRepository;
import vitortheof.com.br.aceleraflix.video.application.VideoLookupService;
import vitortheof.com.br.aceleraflix.video.application.exception.VideoNotFoundException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final VideoLookupService videoLookupService;

    @Transactional
    public void toggleWatchLater(UUID userId, UUID videoId) {
        toggleSystemPlaylist(userId, videoId, PlaylistType.WATCH_LATER, "Assistir mais tarde");
    }

    @Transactional
    public void toggleLikedVideo(UUID userId, UUID videoId) {
        toggleSystemPlaylist(userId, videoId, PlaylistType.LIKED, "Vídeo Curtidos");
    }

    private void toggleSystemPlaylist(UUID userId, UUID videoId, PlaylistType type, String title) {
        playlistRepository.criarPlaylistDeSistemaSeAusente(UUID.randomUUID(), userId, title, type.name());
        Playlist playlist = playlistRepository.buscarParaAtualizar(userId, type)
                .orElseThrow(() -> new IllegalStateException("Playlist de sistema não encontrada após a criação: " + type));

        boolean videoExists = playlist.getVideos().stream()
                .anyMatch(pv -> pv.getVideoId().equals(videoId));

        if (videoExists) {
            playlist.getVideos().removeIf(pv -> pv.getVideoId().equals(videoId));
        } else {
            if (!videoLookupService.existsById(videoId)) {
                throw new VideoNotFoundException(videoId);
            }
            PlaylistVideo newVideo = PlaylistVideo.builder()
                    .playlist(playlist)
                    .videoId(videoId)
                    .build();
            playlist.getVideos().add(newVideo);
        }

        playlistRepository.save(playlist);
    }
}
