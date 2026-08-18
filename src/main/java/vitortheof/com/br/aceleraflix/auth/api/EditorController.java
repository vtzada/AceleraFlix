package vitortheof.com.br.aceleraflix.auth.api;


import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vitortheof.com.br.aceleraflix.auth.application.EditorService;
import vitortheof.com.br.aceleraflix.auth.application.dto.UserPendenteDTO;
import vitortheof.com.br.aceleraflix.auth.domain.Usuario;
import vitortheof.com.br.aceleraflix.auth.infrastructure.security.UserAuth;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/editores")
@Tag(name = "Aprovação de Editores")
public class EditorController {

    private final EditorService editorService;

    @PostMapping("/solicitar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> solicitar(@AuthenticationPrincipal UserAuth userAuth) {
        Usuario usuario = userAuth.getUsuario();
        editorService.solicitarEdicao(usuario.getId());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/pendentes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserPendenteDTO>> listarPendentes() {
        List<UserPendenteDTO> pendentes = editorService.listarPendentes().stream()
                .map(u -> new UserPendenteDTO(u.getId(), u.getNome(), u.getEmail(), u.getCriadoEm()))
                .toList();
        return ResponseEntity.ok(pendentes);
    }

    @PatchMapping("/{usuarioId}/aprovar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> aprovar(@PathVariable UUID usuarioId) {
        editorService.aprovar(usuarioId);
        return  ResponseEntity.noContent().build();
    }

    @PatchMapping("/{usuarioId}/rejeitar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> rejeitar(@PathVariable UUID usuarioId) {
        editorService.rejeitar(usuarioId);
        return  ResponseEntity.noContent().build();
    }

    @PatchMapping("/{usuarioId}/revogar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> revogar(@PathVariable UUID usuarioId) {
        editorService.revogar(usuarioId);
        return  ResponseEntity.noContent().build();
    }

}
