package vitortheof.com.br.aceleraflix.roadmap.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vitortheof.com.br.aceleraflix.roadmap.domain.RoadmapResource;

import java.util.UUID;

@Repository
public interface RoadmapResourceRepository extends JpaRepository<RoadmapResource, UUID> {
}
