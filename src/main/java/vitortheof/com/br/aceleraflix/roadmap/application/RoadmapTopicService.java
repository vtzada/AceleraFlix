package vitortheof.com.br.aceleraflix.roadmap.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vitortheof.com.br.aceleraflix.roadmap.application.dto.request.TopicRequestDTO;
import vitortheof.com.br.aceleraflix.roadmap.application.dto.response.TopicDTO;
import vitortheof.com.br.aceleraflix.roadmap.application.mapper.RoadmapMapper;
import vitortheof.com.br.aceleraflix.roadmap.domain.RoadmapModule;
import vitortheof.com.br.aceleraflix.roadmap.infrastructure.RoadmapModuleRepository;
import vitortheof.com.br.aceleraflix.roadmap.infrastructure.RoadmapTopicRepository;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class RoadmapTopicService {

    private final RoadmapTopicRepository topicRepository;
    private final RoadmapModuleRepository moduleRepository;
    private final RoadmapMapper roadmapMapper;

}
