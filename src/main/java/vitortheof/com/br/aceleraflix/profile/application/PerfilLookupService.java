package vitortheof.com.br.aceleraflix.profile.application;

import vitortheof.com.br.aceleraflix.profile.domain.Perfil;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface PerfilLookupService {

    Map<UUID, Perfil> findByUsuarioIds(Set<UUID> usuarioIds);
}
