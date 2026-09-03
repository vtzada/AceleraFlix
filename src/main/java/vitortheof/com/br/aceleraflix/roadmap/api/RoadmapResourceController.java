package vitortheof.com.br.aceleraflix.roadmap.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vitortheof.com.br.aceleraflix.roadmap.application.RoadmapResourceService;
import vitortheof.com.br.aceleraflix.roadmap.application.dto.request.ResourceRequestDTO;
import vitortheof.com.br.aceleraflix.roadmap.application.dto.response.ResourceDTO;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class RoadmapResourceController {

    private final RoadmapResourceService resourceService;

    @PostMapping("/topics/{topicId}/resources")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResourceDTO> createResource(
            @PathVariable UUID topicId,
            @RequestBody ResourceRequestDTO request) {

        ResourceDTO response = resourceService.createResource(topicId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/resources/{resourceId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResourceDTO> updateResource(
            @PathVariable UUID resourceId,
            @RequestBody ResourceRequestDTO request) {

        return ResponseEntity.ok(resourceService.updateResource(resourceId, request));
    }

    @DeleteMapping("/resources/{resourceId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteResource(@PathVariable UUID resourceId) {
        resourceService.deleteResource(resourceId);
        return ResponseEntity.noContent().build();
    }
}
