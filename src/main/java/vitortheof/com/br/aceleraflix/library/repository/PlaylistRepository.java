package vitortheof.com.br.aceleraflix.library.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import vitortheof.com.br.aceleraflix.library.domain.Playlist;
import vitortheof.com.br.aceleraflix.library.domain.PlaylistType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlaylistRepository extends JpaRepository<Playlist, UUID> {

    @EntityGraph(attributePaths = {"videos"})
    List<Playlist> findAllByUserId(UUID userId);

    @EntityGraph(attributePaths = {"videos"})
    Optional<Playlist> findByUserIdAndType(UUID userId, PlaylistType type);
}
