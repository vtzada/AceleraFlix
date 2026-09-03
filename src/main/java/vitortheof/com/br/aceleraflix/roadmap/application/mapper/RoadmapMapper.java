package vitortheof.com.br.aceleraflix.roadmap.application.mapper;

import org.springframework.stereotype.Component;
import vitortheof.com.br.aceleraflix.roadmap.application.dto.response.ModuleDTO;
import vitortheof.com.br.aceleraflix.roadmap.application.dto.response.ResourceDTO;
import vitortheof.com.br.aceleraflix.roadmap.application.dto.response.RoadmapResponseDTO;
import vitortheof.com.br.aceleraflix.roadmap.application.dto.response.TopicDTO;
import vitortheof.com.br.aceleraflix.roadmap.domain.Roadmap;
import vitortheof.com.br.aceleraflix.roadmap.domain.RoadmapModule;
import vitortheof.com.br.aceleraflix.roadmap.domain.RoadmapResource;
import vitortheof.com.br.aceleraflix.roadmap.domain.RoadmapTopic;

import java.util.Collections;
import java.util.List;

@Component
public class RoadmapMapper {

    public RoadmapResponseDTO toDTO(Roadmap roadmap) {
        if (roadmap == null) return null;

        List<ModuleDTO> modules = (roadmap.getModules() == null) ? Collections.emptyList() :
                roadmap.getModules().stream().map(this::toModuleDTO).toList();

        return new RoadmapResponseDTO(
                roadmap.getId(), roadmap.getTitle(), roadmap.getSlug(),
                roadmap.getDescription(), modules
        );
    }

    public ModuleDTO toModuleDTO(RoadmapModule module) {
        if (module == null) return null;

        List<TopicDTO> topics = (module.getTopics() == null) ? Collections.emptyList() :
                module.getTopics().stream().map(this::toTopicDTO).toList();

        return new ModuleDTO(module.getId(), module.getTitle(), topics);
    }

    private TopicDTO toTopicDTO(RoadmapTopic topic) {
        if (topic == null) return null;

        List<ResourceDTO> resources = (topic.getResources() == null) ? Collections.emptyList() :
                topic.getResources().stream().map(this::toResourceDTO).toList();

        return new TopicDTO(topic.getId(), topic.getTitle(), resources);
    }

    private ResourceDTO toResourceDTO(RoadmapResource resource) {
        if (resource == null) return null;

        return new ResourceDTO(
                resource.getId(), resource.getTitle(), resource.getResourceType(),
                resource.getExternalUrl(), resource.getVideoId()
        );
    }
}