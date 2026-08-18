package vitortheof.com.br.aceleraflix.auth.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import vitortheof.com.br.aceleraflix.auth.domain.StatusEditor;
import vitortheof.com.br.aceleraflix.auth.domain.Usuario;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Usuario> findByStatusEditor(StatusEditor editor);
}
