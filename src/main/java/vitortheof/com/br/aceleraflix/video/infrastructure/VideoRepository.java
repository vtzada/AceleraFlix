package vitortheof.com.br.aceleraflix.video.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import vitortheof.com.br.aceleraflix.video.domain.Video;

import java.util.List;
import java.util.UUID;

public interface VideoRepository extends JpaRepository<Video, UUID> {
    List<Video> findByCategoriaId(UUID categoriaId);
}
