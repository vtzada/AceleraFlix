package vitortheof.com.br.aceleraflix.roadmap.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vitortheof.com.br.aceleraflix.roadmap.application.dto.request.RoadmapRequestDTO;
import vitortheof.com.br.aceleraflix.roadmap.application.dto.response.RoadmapResponseDTO;
import vitortheof.com.br.aceleraflix.roadmap.application.mapper.RoadmapMapper;
import vitortheof.com.br.aceleraflix.roadmap.domain.Roadmap;
import vitortheof.com.br.aceleraflix.roadmap.infrastructure.RoadmapRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoadmapService {

    private final RoadmapRepository roadmapRepository;
    private final RoadmapMapper roadmapMapper;

    public RoadmapResponseDTO getRoadmapBySlug(String slug) {
        return roadmapRepository.findBySlug(slug)
                .map(roadmapMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Roadmap não encontrado para o slug " + slug));
    }

    @Transactional
    public RoadmapResponseDTO createRoadmap(RoadmapRequestDTO request) {
        if (roadmapRepository.findBySlug(request.slug()).isPresent()) {
            throw new RuntimeException("Já existe um roadmap com este slug " + request.slug());
        }

        Roadmap roadmap = Roadmap.builder()
                .title(request.title())
                .slug(request.slug())
                .description(request.description())
                .build();

        Roadmap savedRoadmap = roadmapRepository.save(roadmap);
        return roadmapMapper.toDTO(savedRoadmap);
    }

    @Transactional
    public RoadmapResponseDTO updateRoadmap(UUID id, RoadmapRequestDTO request) {
        Roadmap roadmap = roadmapRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Roadmap não encontrado"));
        roadmap.setTitle(request.title());
        roadmap.setSlug(request.slug());
        roadmap.setDescription(request.description());

        return roadmapMapper.toDTO(roadmapRepository.save(roadmap));
    }

    @Transactional
    public void deleteRoadmap(UUID id) {
        Roadmap roadmap = roadmapRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Roadmap não encontrado: " + id));

        roadmapRepository.delete(roadmap);
    }

    @Transactional(readOnly = true)
    public List<RoadmapResponseDTO> getAllRoadmaps() {
        return roadmapRepository.findAll().stream()
                .map(roadmapMapper::toDTO)
                .toList();
    }
}
