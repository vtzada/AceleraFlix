package vitortheof.com.br.aceleraflix.roadmap.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vitortheof.com.br.aceleraflix.roadmap.domain.RoadmapTopic;

import java.util.UUID;

@Repository
public interface RoadmapTopicRepository extends JpaRepository<RoadmapTopic, UUID> {
}
