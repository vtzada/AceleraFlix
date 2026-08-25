package vitortheof.com.br.aceleraflix.profile.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vitortheof.com.br.aceleraflix.auth.infrastructure.security.UserAuth;
import vitortheof.com.br.aceleraflix.profile.application.PerfilService;
import vitortheof.com.br.aceleraflix.profile.application.dto.PerfilRequest;
import vitortheof.com.br.aceleraflix.profile.application.dto.PerfilResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/u")
@Tag(name = "Perfil")
public class PerfilController {

    private final PerfilService perfilService;

    @PostMapping
    public ResponseEntity<PerfilResponse> criar(
            @Valid @RequestBody PerfilRequest request,
            @AuthenticationPrincipal UserAuth usuario
    ) {
        PerfilResponse response = perfilService.criarPerfil(usuario.getUsuario().getId(),  request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping
    public ResponseEntity<PerfilResponse> atualizar(
            @Valid @RequestBody PerfilRequest request,
            @AuthenticationPrincipal UserAuth usuario
    ) {
        PerfilResponse response = perfilService.atualizarPerfil(usuario.getUsuario().getId(), request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<PerfilResponse> buscar(
            @AuthenticationPrincipal UserAuth usuario
    ) {
        PerfilResponse response = perfilService.buscarPerfil(usuario.getUsuario().getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<PerfilResponse> buscarMeuPerfil(@AuthenticationPrincipal UserAuth usuario) {
        PerfilResponse response = perfilService.buscarMeuPerfil(usuario.getUsuario().getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{username}")
    public ResponseEntity<PerfilResponse> buscarPorUsername(@PathVariable String username) {
        PerfilResponse response = perfilService.buscarPorUsername(username);
        return ResponseEntity.ok(response);
    }
}
