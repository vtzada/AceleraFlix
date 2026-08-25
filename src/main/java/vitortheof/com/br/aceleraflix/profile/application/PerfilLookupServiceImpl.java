package vitortheof.com.br.aceleraflix.profile.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vitortheof.com.br.aceleraflix.profile.domain.Perfil;
import vitortheof.com.br.aceleraflix.profile.infrastructure.PerfilRepository;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PerfilLookupServiceImpl implements PerfilLookupService {

    private final PerfilRepository perfilRepository;

    @Override
    public Map<UUID, Perfil> findByUsuarioIds(Set<UUID> usuarioIds) {
        if (usuarioIds == null || usuarioIds.isEmpty()) {
            return Map.of();
        }
        return perfilRepository.findByUsuarioIdIn(usuarioIds).stream()
                .collect(Collectors.toMap(Perfil::getUsuarioId, Function.identity()));
    }
}
