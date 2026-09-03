package vitortheof.com.br.aceleraflix.roadmap.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vitortheof.com.br.aceleraflix.roadmap.application.dto.request.ResourceRequestDTO;
import vitortheof.com.br.aceleraflix.roadmap.application.dto.response.ResourceDTO;
import vitortheof.com.br.aceleraflix.roadmap.application.mapper.RoadmapMapper;
import vitortheof.com.br.aceleraflix.roadmap.domain.RoadmapResource;
import vitortheof.com.br.aceleraflix.roadmap.domain.RoadmapTopic;
import vitortheof.com.br.aceleraflix.roadmap.infrastructure.RoadmapResourceRepository;
import vitortheof.com.br.aceleraflix.roadmap.infrastructure.RoadmapTopicRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoadmapResourceService {

    private final RoadmapTopicRepository topicRepository;
    private final RoadmapResourceRepository resourceRepository;
    private final RoadmapMapper roadmapMapper;

    @Transactional
    public ResourceDTO createResource(UUID topicId, ResourceRequestDTO request) {
        RoadmapTopic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic with id " + topicId + " not found"));

        RoadmapResource resource = RoadmapResource.builder()
                .title(request.title())
                .resourceType(request.type())
                .externalUrl(request.externalUrl())
                .videoId(request.videoId())
                .orderIndex(request.orderIndex())
                .topic(topic)
                .build();

        RoadmapResource savedResource = resourceRepository.save(resource);
        return roadmapMapper.toResourceDTO(savedResource);
    }

    @Transactional
    public ResourceDTO updateResource(UUID resourceId, ResourceRequestDTO request) {
        RoadmapResource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource with id " + resourceId + " not found"));

        resource.setTitle(request.title());
        resource.setResourceType(request.type());
        resource.setExternalUrl(request.externalUrl());
        resource.setVideoId(request.videoId());
        resource.setOrderIndex(request.orderIndex());

        return roadmapMapper.toResourceDTO(resource);
    }

    @Transactional
    public void deleteResource(UUID resourceId) {
        if (!resourceRepository.existsById(resourceId)) {
            throw new RuntimeException("Resource not found: " + resourceId);
        }
        resourceRepository.deleteById(resourceId);
    }
}

