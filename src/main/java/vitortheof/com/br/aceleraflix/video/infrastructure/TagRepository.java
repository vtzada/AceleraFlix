package vitortheof.com.br.aceleraflix.video.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import vitortheof.com.br.aceleraflix.video.domain.Tag;

import java.util.Optional;
import java.util.UUID;

public interface TagRepository extends JpaRepository<Tag, UUID> {
    Optional<Tag> findByNome(String nome);
}
