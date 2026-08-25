package vitortheof.com.br.aceleraflix.video.infrastructure;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import vitortheof.com.br.aceleraflix.video.domain.Video;

import java.util.UUID;

public interface VideoRepository extends JpaRepository<Video, UUID> {
    Page<Video> findByCategoriaId(UUID categoriaId, Pageable pageable);
    Page<Video> findByCriadoPor(UUID criadoPor, Pageable pageable);
    Page<Video> findByTituloContainingIgnoreCaseOrDescricaoContainingIgnoreCaseOrTags_NomeContainingIgnoreCase(
            String titulo, String descricao, String tagNome, Pageable pageable);
    boolean existsByVideoExternoId(String videoExternoId);
}
