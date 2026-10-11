package vitortheof.com.br.aceleraflix.engagement.infrastructure;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vitortheof.com.br.aceleraflix.engagement.domain.Playlist;
import vitortheof.com.br.aceleraflix.engagement.domain.PlaylistType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlaylistRepository extends JpaRepository<Playlist, UUID> {

    @EntityGraph(attributePaths = {"videos"})
    List<Playlist> findAllByUserId(UUID userId);

    @EntityGraph(attributePaths = {"videos"})
    Optional<Playlist> findByUserIdAndType(UUID userId, PlaylistType type);

    // Cria a playlist de sistema só se ainda não existir. O ON CONFLICT usa o índice único parcial da V15,
    // então duas requisições simultâneas não geram duplicada nem violação de unicidade.
    @Modifying
    @Query(value = """
            INSERT INTO playlist (id, user_id, title, type)
            VALUES (:id, :userId, :title, :type)
            ON CONFLICT (user_id, type) WHERE type IN ('WATCH_LATER', 'LIKED') DO NOTHING
            """, nativeQuery = true)
    void criarPlaylistDeSistemaSeAusente(@Param("id") UUID id, @Param("userId") UUID userId,
                                         @Param("title") String title, @Param("type") String type);

    // Trava a linha da playlist até o fim da transação: toggles simultâneos do mesmo usuário são serializados.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Playlist p where p.userId = :userId and p.type = :type")
    Optional<Playlist> buscarParaAtualizar(@Param("userId") UUID userId, @Param("type") PlaylistType type);
}
