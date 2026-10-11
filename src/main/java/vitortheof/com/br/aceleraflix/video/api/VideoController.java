package vitortheof.com.br.aceleraflix.video.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vitortheof.com.br.aceleraflix.auth.domain.RoleUsuario;
import vitortheof.com.br.aceleraflix.auth.infrastructure.security.UserAuth;
import vitortheof.com.br.aceleraflix.video.application.VideoService;
import vitortheof.com.br.aceleraflix.video.application.dto.VideoRequest;
import vitortheof.com.br.aceleraflix.video.application.dto.VideoResponse;
import vitortheof.com.br.aceleraflix.video.application.dto.VideoUpdateRequest;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/videos")
@Tag(name = "videos")
public class VideoController {

    private final VideoService videoService;

    @GetMapping
    public ResponseEntity<Page<VideoResponse>> listar(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) UUID criadoPor,
            @PageableDefault(size = 20, sort = "criadoEm", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal UserAuth usuarioAutenticado) {

        UUID usuarioId = usuarioIdOuNull(usuarioAutenticado);

        Page<VideoResponse> result;
        if (q != null && !q.isBlank()) {
            result = videoService.search(q.trim(), pageable, usuarioId);
        } else if (criadoPor != null) {
            result = videoService.findByCreator(criadoPor, pageable, usuarioId);
        } else {
            result = videoService.findAll(pageable, usuarioId);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VideoResponse> findById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserAuth usuarioAutenticado) {
        return ResponseEntity.ok(videoService.findById(id, usuarioIdOuNull(usuarioAutenticado)));
    }
    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<Page<VideoResponse>> listarPorCategoria(
            @PathVariable UUID categoriaId,
            @PageableDefault(size = 20, sort = "criadoEm", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal UserAuth usuarioAutenticado) {
        return ResponseEntity.ok(
                videoService.findByCategory(categoriaId, pageable, usuarioIdOuNull(usuarioAutenticado)));
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
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('EDITOR') or hasRole('ADMIN')")
    public ResponseEntity<VideoResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody VideoUpdateRequest request,
            @AuthenticationPrincipal UserAuth usuarioAutenticado) {

        UUID usuarioId = usuarioAutenticado.getUsuario().getId();
        boolean isAdmin = usuarioAutenticado.getUsuario().getRole() == RoleUsuario.ADMIN;

        VideoResponse response = videoService.update(id, request, usuarioId, isAdmin);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('EDITOR') or hasRole('ADMIN')")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserAuth usuarioAutenticado) {

        UUID usuarioId = usuarioAutenticado.getUsuario().getId();
        boolean isAdmin = usuarioAutenticado.getUsuario().getRole() == RoleUsuario.ADMIN;

        videoService.delete(id, usuarioId, isAdmin);
        return ResponseEntity.noContent().build();
    }

    // Leitura é pública: sem sessão o principal vem null e a nota do usuário fica null.
    private UUID usuarioIdOuNull(UserAuth usuarioAutenticado) {
        return usuarioAutenticado != null ? usuarioAutenticado.getUsuario().getId() : null;
    }
}
