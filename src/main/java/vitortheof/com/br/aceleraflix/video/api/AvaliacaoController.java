package vitortheof.com.br.aceleraflix.video.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vitortheof.com.br.aceleraflix.auth.infrastructure.security.UserAuth;
import vitortheof.com.br.aceleraflix.video.application.AvaliacaoService;
import vitortheof.com.br.aceleraflix.video.application.dto.AvaliacaoRequest;
import vitortheof.com.br.aceleraflix.video.application.dto.AvaliacaoResumoResponse;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/videos/{id}/avaliacao")
@Tag(name = "avaliacoes")
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;

    @PutMapping
    public ResponseEntity<AvaliacaoResumoResponse> avaliar(
            @PathVariable UUID id,
            @Valid @RequestBody AvaliacaoRequest request,
            @AuthenticationPrincipal UserAuth usuarioAutenticado) {

        UUID usuarioId = usuarioAutenticado.getUsuario().getId();
        return ResponseEntity.ok(avaliacaoService.avaliar(id, usuarioId, request.nota().intValueExact()));
    }

    @DeleteMapping
    public ResponseEntity<AvaliacaoResumoResponse> remover(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserAuth usuarioAutenticado) {

        UUID usuarioId = usuarioAutenticado.getUsuario().getId();
        return ResponseEntity.ok(avaliacaoService.remover(id, usuarioId));
    }
}
