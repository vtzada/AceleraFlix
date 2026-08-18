package vitortheof.com.br.aceleraflix.auth.application;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import vitortheof.com.br.aceleraflix.auth.application.dto.UserDTO;
import vitortheof.com.br.aceleraflix.auth.domain.StatusEditor;
import vitortheof.com.br.aceleraflix.auth.domain.Usuario;
import vitortheof.com.br.aceleraflix.auth.infrastructure.UsuarioRepository;

import java.util.UUID;

@Service
@AllArgsConstructor
public class UserLookupServiceImpl implements UserLookupService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDTO findById(UUID userId) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return new UserDTO(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getRole());
    }

    public boolean isEditorApproved(UUID userId) {
        return usuarioRepository.findById(userId)
                .map(u -> u.getStatusEditor() == StatusEditor.APROVADO)
                .orElse(false);
    }
}
