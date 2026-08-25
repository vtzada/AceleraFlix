package vitortheof.com.br.aceleraflix.profile.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vitortheof.com.br.aceleraflix.profile.domain.Perfil;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface PerfilRepository extends JpaRepository<Perfil, UUID> {

    Optional<Perfil> findByUsername(String username);
    Optional<Perfil> findByUsuarioId(UUID usuarioId);
    List<Perfil> findByUsuarioIdIn(Set<UUID> usuarioIds);
    boolean existsByUsername(String username);

}
