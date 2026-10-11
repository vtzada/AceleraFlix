package vitortheof.com.br.aceleraflix.video.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vitortheof.com.br.aceleraflix.video.application.dto.AvaliacaoResumoResponse;
import vitortheof.com.br.aceleraflix.video.application.exception.VideoNotFoundException;
import vitortheof.com.br.aceleraflix.video.infrastructure.AvaliacaoVideoRepository;
import vitortheof.com.br.aceleraflix.video.infrastructure.AvaliacaoVideoRepository.MediaPorVideo;
import vitortheof.com.br.aceleraflix.video.infrastructure.AvaliacaoVideoRepository.NotaPorVideo;
import vitortheof.com.br.aceleraflix.video.infrastructure.VideoRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class AvaliacaoService {

    private final AvaliacaoVideoRepository avaliacaoVideoRepository;
    private final VideoRepository videoRepository;

    @Transactional
    public AvaliacaoResumoResponse avaliar(UUID videoId, UUID usuarioId, int nota) {
        verificarVideoExiste(videoId);
        avaliacaoVideoRepository.salvarNota(videoId, usuarioId, nota);
        return resumir(videoId, usuarioId);
    }

    @Transactional
    public AvaliacaoResumoResponse remover(UUID videoId, UUID usuarioId) {
        verificarVideoExiste(videoId);
        avaliacaoVideoRepository.removerNota(videoId, usuarioId);
        return resumir(videoId, usuarioId);
    }

    @Transactional(readOnly = true)
    public AvaliacaoResumoResponse resumir(UUID videoId, UUID usuarioId) {
        return resumirPorVideos(Set.of(videoId), usuarioId)
                .getOrDefault(videoId, AvaliacaoResumoResponse.SEM_AVALIACOES);
    }

    /**
     * Resumo em lote (no máximo duas consultas, independente da quantidade de vídeos).
     * Vídeos sem avaliações não aparecem no mapa.
     */
    @Transactional(readOnly = true)
    public Map<UUID, AvaliacaoResumoResponse> resumirPorVideos(Collection<UUID> videoIds, UUID usuarioId) {
        if (videoIds == null || videoIds.isEmpty()) {
            return Map.of();
        }

        Map<UUID, Integer> notasDoUsuario = usuarioId == null
                ? Map.of()
                : avaliacaoVideoRepository.buscarNotasDoUsuario(usuarioId, videoIds).stream()
                        .collect(Collectors.toMap(NotaPorVideo::getVideoId, NotaPorVideo::getNota));

        Map<UUID, AvaliacaoResumoResponse> resumos = new HashMap<>();
        for (MediaPorVideo agregado : avaliacaoVideoRepository.agregarPorVideos(videoIds)) {
            resumos.put(agregado.getVideoId(), new AvaliacaoResumoResponse(
                    arredondar(agregado.getMedia()),
                    agregado.getTotal(),
                    notasDoUsuario.get(agregado.getVideoId())));
        }
        return resumos;
    }

    private void verificarVideoExiste(UUID videoId) {
        if (!videoRepository.existsById(videoId)) {
            throw new VideoNotFoundException(videoId);
        }
    }

    private Double arredondar(Double media) {
        return media == null
                ? null
                : BigDecimal.valueOf(media).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }
}
