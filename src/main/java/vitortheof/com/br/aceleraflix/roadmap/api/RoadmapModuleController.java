package vitortheof.com.br.aceleraflix.roadmap.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vitortheof.com.br.aceleraflix.roadmap.application.RoadmapModuleService;
import vitortheof.com.br.aceleraflix.roadmap.application.dto.request.ModuleRequestDTO;
import vitortheof.com.br.aceleraflix.roadmap.application.dto.response.ModuleDTO;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class RoadmapModuleController {

    private final RoadmapModuleService moduleService;

    @PostMapping("/roadmaps/{roadmapId}/modules")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ModuleDTO> createModule(@PathVariable UUID roadmapId,
                                                  @RequestBody ModuleRequestDTO request) {
        ModuleDTO response = moduleService.createModule(roadmapId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/modules/{moduleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ModuleDTO> updateModule(@PathVariable UUID moduleId,
                                                  @RequestBody ModuleRequestDTO request) {
        ModuleDTO response = moduleService.updateModule(moduleId, request);
        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping("/modules/{moduleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteModule(@PathVariable UUID moduleId) {
        moduleService.deleteModule(moduleId);
        return ResponseEntity.noContent().build();
    }
}
