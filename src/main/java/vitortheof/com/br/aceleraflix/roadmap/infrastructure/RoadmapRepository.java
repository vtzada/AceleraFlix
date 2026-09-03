package vitortheof.com.br.aceleraflix.roadmap.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vitortheof.com.br.aceleraflix.roadmap.domain.Roadmap;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoadmapRepository extends JpaRepository<Roadmap, UUID> {

    Optional<Roadmap> findBySlug(String slug);
}
