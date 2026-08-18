package vitortheof.com.br.aceleraflix.auth.application.dto;

import vitortheof.com.br.aceleraflix.auth.domain.RoleUsuario;

import java.util.UUID;

public record UserDTO (UUID id,
                       String nome,
                       String email,
                       RoleUsuario role) {
}
