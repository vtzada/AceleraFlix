package vitortheof.com.br.aceleraflix.video.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vitortheof.com.br.aceleraflix.auth.infrastructure.security.UserAuth;
import vitortheof.com.br.aceleraflix.video.application.VideoService;
import vitortheof.com.br.aceleraflix.video.application.dto.VideoRequest;
import vitortheof.com.br.aceleraflix.video.application.dto.VideoResponse;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/videos")
@Tag(name = "videos")
public class VideoController {

    private final VideoService videoService;

    @GetMapping
    public ResponseEntity<List<VideoResponse>> listarTodos() {
        return ResponseEntity.ok(videoService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VideoResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(videoService.findById(id));
    }
    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<VideoResponse>> listarPorCategoria(@PathVariable UUID categoriaId) {
        return ResponseEntity.ok(videoService.findByCategory(categoriaId));
    }

    @PostMapping
    @PreAuthorize("hasRole('EDITOR') or hasRole('ADMIN')")
    public ResponseEntity<VideoResponse> criar(
            @Valid @RequestBody VideoRequest request,
            @AuthenticationPrincipal UserAuth usuarioAutenticado) {

        UUID criadoPor = usuarioAutenticado.getUsuario().getId();
        VideoResponse response = videoService.create(request, criadoPor);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
