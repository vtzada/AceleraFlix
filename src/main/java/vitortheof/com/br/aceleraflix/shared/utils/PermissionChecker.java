package vitortheof.com.br.aceleraflix.shared.utils;

import org.springframework.stereotype.Component;
import vitortheof.com.br.aceleraflix.shared.exception.AccessDeniedException;

import java.util.UUID;

@Component
public class PermissionChecker {

    public void verificarPerm(UUID criadoPor, UUID usuarioId, boolean isAdmin) {
        if (!isAdmin && !criadoPor.equals(usuarioId)) {
            throw new AccessDeniedException("Você não tem permissão para modificar este vídeo");
        }
    }
}
