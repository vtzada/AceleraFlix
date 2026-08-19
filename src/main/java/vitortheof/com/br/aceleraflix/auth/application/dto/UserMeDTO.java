package vitortheof.com.br.aceleraflix.auth.application.dto;

import vitortheof.com.br.aceleraflix.auth.domain.RoleUsuario;
import vitortheof.com.br.aceleraflix.auth.domain.StatusEditor;

import java.util.UUID;

public record UserMeDTO(UUID id,
                        String nome,
                        String email,
                        RoleUsuario role,
                        StatusEditor statusEditor) {
}