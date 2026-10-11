package vitortheof.com.br.aceleraflix.video.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vitortheof.com.br.aceleraflix.video.domain.AvaliacaoVideo;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface AvaliacaoVideoRepository extends JpaRepository<AvaliacaoVideo, UUID> {

    // Upsert atômico: a unicidade (usuario_id, video_id) garante uma avaliação por usuário
    // mesmo com requisições concorrentes.
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            INSERT INTO avaliacao_video (id, video_id, usuario_id, nota, criado_em, atualizado_em)
            VALUES (gen_random_uuid(), :videoId, :usuarioId, :nota, now(), now())
            ON CONFLICT (usuario_id, video_id)
            DO UPDATE SET nota = EXCLUDED.nota, atualizado_em = now()
            """, nativeQuery = true)
    void salvarNota(@Param("videoId") UUID videoId, @Param("usuarioId") UUID usuarioId, @Param("nota") int nota);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from AvaliacaoVideo a where a.videoId = :videoId and a.usuarioId = :usuarioId")
    void removerNota(@Param("videoId") UUID videoId, @Param("usuarioId") UUID usuarioId);

    @Query("""
            select a.videoId as videoId, avg(a.nota) as media, count(a) as total
            from AvaliacaoVideo a
            where a.videoId in :videoIds
            group by a.videoId
            """)
    List<MediaPorVideo> agregarPorVideos(@Param("videoIds") Collection<UUID> videoIds);

    @Query("""
            select a.videoId as videoId, a.nota as nota
            from AvaliacaoVideo a
            where a.usuarioId = :usuarioId and a.videoId in :videoIds
            """)
    List<NotaPorVideo> buscarNotasDoUsuario(@Param("usuarioId") UUID usuarioId,
                                            @Param("videoIds") Collection<UUID> videoIds);

    interface MediaPorVideo {
        UUID getVideoId();
        Double getMedia();
        Long getTotal();
    }

    interface NotaPorVideo {
        UUID getVideoId();
        Integer getNota();
    }
}
