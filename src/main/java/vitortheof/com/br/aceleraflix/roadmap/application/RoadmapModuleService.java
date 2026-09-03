package vitortheof.com.br.aceleraflix.roadmap.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vitortheof.com.br.aceleraflix.roadmap.application.dto.request.ModuleRequestDTO;
import vitortheof.com.br.aceleraflix.roadmap.application.dto.response.ModuleDTO;
import vitortheof.com.br.aceleraflix.roadmap.application.mapper.RoadmapMapper;
import vitortheof.com.br.aceleraflix.roadmap.domain.Roadmap;
import vitortheof.com.br.aceleraflix.roadmap.domain.RoadmapModule;
import vitortheof.com.br.aceleraflix.roadmap.infrastructure.RoadmapModuleRepository;
import vitortheof.com.br.aceleraflix.roadmap.infrastructure.RoadmapRepository;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class RoadmapModuleService {

    private final RoadmapModuleRepository roadmapModuleRepository;
    private final RoadmapMapper roadmapMapper;
    private final RoadmapRepository roadmapRepository;

    @Transactional
    public ModuleDTO createModule(UUID roadmapId, ModuleRequestDTO request) {
        Roadmap roadmap = roadmapRepository.findById(roadmapId).
                orElseThrow(() -> new RuntimeException("Roadmap with id " + roadmapId + " not found"));

        RoadmapModule module = RoadmapModule.builder()
                .title(request.title())
                .orderIndex(request.orderIndex())
                .roadmap(roadmap)
                .build();

        RoadmapModule savedModule = roadmapModuleRepository.save(module);

        return  roadmapMapper.toModuleDTO(savedModule);
    }

    @Transactional
    public ModuleDTO updateModule(UUID moduleId, ModuleRequestDTO request) {
        RoadmapModule module = roadmapModuleRepository.findById(moduleId).
                orElseThrow(() -> new RuntimeException("Module with id " + moduleId + " not found"));

        module.setTitle(request.title());
        module.setOrderIndex(request.orderIndex());

        return roadmapMapper.toModuleDTO(module);
    }

    @Transactional
    public void deleteModule(UUID moduleId) {
        if (!roadmapModuleRepository.existsById(moduleId)) {
            throw new RuntimeException("Módulo não encontrado: " + moduleId);
        }
        roadmapModuleRepository.deleteById(moduleId);
    }
}

