package vitortheof.com.br.aceleraflix.roadmap.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vitortheof.com.br.aceleraflix.roadmap.application.RoadmapService;
import vitortheof.com.br.aceleraflix.roadmap.application.dto.request.RoadmapRequestDTO;
import vitortheof.com.br.aceleraflix.roadmap.application.dto.response.ModuleDTO;
import vitortheof.com.br.aceleraflix.roadmap.application.dto.response.RoadmapResponseDTO;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/roadmap")
@RequiredArgsConstructor
@Tag(name = "Roadmap")
public class RoadmapController {

    private final RoadmapService roadmapService;

    @GetMapping("/{slug}")
    public ResponseEntity<RoadmapResponseDTO> getRoadmapBySlug(@PathVariable String slug) {
        RoadmapResponseDTO response = roadmapService.getRoadmapBySlug(slug);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<RoadmapResponseDTO> createRoadmap(@Valid @RequestBody RoadmapRequestDTO request) {
        RoadmapResponseDTO response = roadmapService.createRoadmap(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoadmapResponseDTO> updateRoadmap(@PathVariable UUID id, @RequestBody RoadmapRequestDTO roadmapRequestDTO) {
        RoadmapResponseDTO response = roadmapService.updateRoadmap(id, roadmapRequestDTO);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoadmap(@PathVariable UUID id) {
        roadmapService.deleteRoadmap(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping
    public ResponseEntity<List<RoadmapResponseDTO>> getAllRoadmaps() {
        return ResponseEntity.ok(roadmapService.getAllRoadmaps());
   }
}

