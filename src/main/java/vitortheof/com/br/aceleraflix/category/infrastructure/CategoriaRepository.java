package vitortheof.com.br.aceleraflix.category.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import vitortheof.com.br.aceleraflix.category.domain.Categoria;

import java.util.Optional;
import java.util.UUID;

public interface CategoriaRepository extends JpaRepository<Categoria, UUID> {

    boolean existsBySlug(String slug);
    Optional<Categoria> findBySlug(String slug);
}
