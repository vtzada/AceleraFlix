package vitortheof.com.br.aceleraflix.library.application.mapper;

import org.springframework.stereotype.Component;
import vitortheof.com.br.aceleraflix.library.application.dto.response.PlaylistResponseDTO;
import vitortheof.com.br.aceleraflix.library.application.dto.response.PlaylistVideoResponse;
import vitortheof.com.br.aceleraflix.library.domain.Playlist;

@Component
public class PlaylistMapper {

    public static PlaylistResponseDTO toDTO(Playlist playlist) {
        var videoDTO = playlist.getVideos().stream()
                .map(pv -> new PlaylistVideoResponse(pv.getVideoId(), pv.getAddedAt()))
                .toList();

        return new PlaylistResponseDTO(
                playlist.getId(),
                playlist.getTitle(),
                playlist.getType(),
                videoDTO
        );
    }

}
