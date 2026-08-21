package vitortheof.com.br.aceleraflix.auth.application;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import vitortheof.com.br.aceleraflix.auth.application.dto.UserDTO;
import vitortheof.com.br.aceleraflix.auth.application.mapper.UserMapper;
import vitortheof.com.br.aceleraflix.auth.domain.StatusEditor;
import vitortheof.com.br.aceleraflix.auth.domain.Usuario;
import vitortheof.com.br.aceleraflix.auth.infrastructure.UsuarioRepository;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserLookupServiceImpl implements UserLookupService {

    private final UsuarioRepository usuarioRepository;
    private final UserMapper userMapper;

    @Override
    public UserDTO findById(UUID userId) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return userMapper.toDTO(usuario);
    }

    public boolean isEditorApproved(UUID userId) {
        return usuarioRepository.findById(userId)
                .map(u -> u.getStatusEditor() == StatusEditor.APROVADO)
                .orElse(false);
    }

    public Map<UUID, UserDTO> findAllById(Set<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        return usuarioRepository.findAllById(ids).stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toMap(UserDTO::id, dto -> dto));
    }
}
