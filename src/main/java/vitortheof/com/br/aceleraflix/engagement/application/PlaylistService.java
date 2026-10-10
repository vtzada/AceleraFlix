package vitortheof.com.br.aceleraflix.engagement.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vitortheof.com.br.aceleraflix.engagement.domain.Playlist;
import vitortheof.com.br.aceleraflix.engagement.domain.PlaylistType;
import vitortheof.com.br.aceleraflix.engagement.domain.PlaylistVideo;
import vitortheof.com.br.aceleraflix.engagement.infrastructure.PlaylistRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlaylistService {

    private final PlaylistRepository playlistRepository;

    @Transactional
    public void toggleWatchLater(UUID userId, UUID videoId) {
        toggleSystemPlaylist(userId, videoId, PlaylistType.WATCH_LATER, "Assistir mais tarde");
    }

    public void toggleLikedVideo(UUID userId, UUID videoId) {
        toggleSystemPlaylist(userId, videoId, PlaylistType.LIKED, "Vídeo Curtidos");
    }

    private void toggleSystemPlaylist(UUID userId, UUID videoId, PlaylistType type, String title) {
        Playlist playlist = playlistRepository.findByUserIdAndType(userId, type)
                .orElseGet(() -> createPlaylist(userId, title, type));

        boolean videoExists = playlist.getVideos().stream()
                .anyMatch(pv -> pv.getVideoId().equals(videoId));

        if (videoExists) {
            playlist.getVideos().removeIf(pv -> pv.getVideoId().equals(videoId));
        } else {
            PlaylistVideo newVideo = PlaylistVideo.builder()
                    .playlist(playlist)
                    .videoId(videoId)
                    .build();
            playlist.getVideos().add(newVideo);
        }

        playlistRepository.save(playlist);
    }

    private Playlist createPlaylist(UUID userId, String title, PlaylistType type) {
        return Playlist.builder()
                .userId(userId)
                .title(title)
                .type(type)
                .build();
    }

}
