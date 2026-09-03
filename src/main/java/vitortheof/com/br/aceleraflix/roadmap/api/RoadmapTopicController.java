package vitortheof.com.br.aceleraflix.roadmap.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vitortheof.com.br.aceleraflix.roadmap.application.RoadmapTopicService;
import vitortheof.com.br.aceleraflix.roadmap.application.dto.request.TopicRequestDTO;
import vitortheof.com.br.aceleraflix.roadmap.application.dto.response.TopicDTO;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class RoadmapTopicController {

    private final RoadmapTopicService topicService;

    @PostMapping("/modules/{moduleId}/topics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TopicDTO> createTopic(@PathVariable UUID moduleId,
                                                @RequestBody TopicRequestDTO request) {
        TopicDTO response = topicService.createTopic(moduleId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/topics/{topicId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TopicDTO> updateTopic(
            @PathVariable UUID topicId,
            @RequestBody TopicRequestDTO request) {

        return ResponseEntity.ok(topicService.updateTopic(topicId, request));
    }
    @DeleteMapping("/topics/{topicId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTopic(@PathVariable UUID topicId) {
        topicService.deleteTopic(topicId);
        return ResponseEntity.noContent().build();
    }
}
