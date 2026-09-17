package vitortheof.com.br.aceleraflix.engagement.infrastructure;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import vitortheof.com.br.aceleraflix.engagement.domain.Playlist;
import vitortheof.com.br.aceleraflix.engagement.domain.PlaylistType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlaylistRepository extends JpaRepository<Playlist, UUID> {

    @EntityGraph(attributePaths = {"videos"})
    List<Playlist> findAllByUserId(UUID userId);

    @EntityGraph(attributePaths = {"videos"})
    Optional<Playlist> findByUserIdAndType(UUID userId, PlaylistType type);
}
