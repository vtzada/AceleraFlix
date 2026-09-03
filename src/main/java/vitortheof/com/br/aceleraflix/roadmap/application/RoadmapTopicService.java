package vitortheof.com.br.aceleraflix.roadmap.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vitortheof.com.br.aceleraflix.roadmap.application.dto.request.TopicRequestDTO;
import vitortheof.com.br.aceleraflix.roadmap.application.dto.response.TopicDTO;
import vitortheof.com.br.aceleraflix.roadmap.application.mapper.RoadmapMapper;
import vitortheof.com.br.aceleraflix.roadmap.domain.RoadmapModule;
import vitortheof.com.br.aceleraflix.roadmap.domain.RoadmapTopic;
import vitortheof.com.br.aceleraflix.roadmap.infrastructure.RoadmapModuleRepository;
import vitortheof.com.br.aceleraflix.roadmap.infrastructure.RoadmapTopicRepository;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class RoadmapTopicService {

    private final RoadmapTopicRepository topicRepository;
    private final RoadmapModuleRepository moduleRepository;
    private final RoadmapMapper roadmapMapper;


    @Transactional
    public TopicDTO createTopic(UUID moduleId, TopicRequestDTO request) {
        RoadmapModule module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module Not Found"));

        RoadmapTopic topic = RoadmapTopic.builder()
                .title(request.title())
                .orderIndex(request.orderIndex())
                .module(module)
                .build();

        RoadmapTopic savedTopic =  topicRepository.save(topic);
        return roadmapMapper.toTopicDTO(savedTopic);
    }


    @Transactional
    public TopicDTO updateTopic(UUID topicId, TopicRequestDTO request) {
        RoadmapTopic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic Not Found"));

        topic.setTitle(request.title());
        topic.setOrderIndex(request.orderIndex());

        return roadmapMapper.toTopicDTO(topic);
    }

    @Transactional
    public void deleteTopic(UUID topicId) {
        if (!topicRepository.existsById(topicId)) {
            throw new RuntimeException("Topic not found: " + topicId);
        }
        topicRepository.deleteById(topicId);
    }
}
