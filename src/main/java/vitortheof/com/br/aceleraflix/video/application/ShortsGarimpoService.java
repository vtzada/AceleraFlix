package vitortheof.com.br.aceleraflix.video.application;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vitortheof.com.br.aceleraflix.video.application.dto.ShortsGarimpoResult;
import vitortheof.com.br.aceleraflix.video.domain.Plataforma;
import vitortheof.com.br.aceleraflix.video.domain.StatusVideo;
import vitortheof.com.br.aceleraflix.video.domain.Tag;
import vitortheof.com.br.aceleraflix.video.domain.Video;
import vitortheof.com.br.aceleraflix.video.infrastructure.VideoRepository;
import vitortheof.com.br.aceleraflix.video.infrastructure.youtube.YoutubeClient;
import vitortheof.com.br.aceleraflix.video.infrastructure.youtube.YoutubeVideoDetalhes;

import java.text.Normalizer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShortsGarimpoService {

    private static final int MAX_SHORT_SEGUNDOS = 180;

    private final VideoRepository videoRepository;
    private final YoutubeClient youtubeClient;
    private final TagResolver tagResolver;

    @Value("${youtube.api.shorts.canais:}")
    private List<String> canais;

    @Value("${youtube.api.shorts.keywords:}")
    private List<String> keywords;

    @Value("${youtube.api.shorts.max-por-canal:50}")
    private int maxPorCanal;

    @Transactional
    public ShortsGarimpoResult garimpar(UUID adminId) {
        int importados = 0;
        int duplicados = 0;
        int foraDoFiltro = 0;

        List<String> keywordsNormalizadas = keywords.stream()
                .map(ShortsGarimpoService::normalizar)
                .filter(k -> !k.isBlank())
                .toList();

        for (String handle : canais) {
            if (handle == null || handle.isBlank()) {
                continue;
            }

            var playlistId = youtubeClient.buscarUploadsPlaylistPorHandle(handle.trim());
            if (playlistId.isEmpty()) {
                continue;
            }

            List<YoutubeVideoDetalhes> recentes = youtubeClient.buscarUploadsRecentes(
                    playlistId.get(), maxPorCanal);

            for (YoutubeVideoDetalhes detalhe : recentes) {
                if (detalhe.duracaoSegundos() == null || detalhe.duracaoSegundos() > MAX_SHORT_SEGUNDOS) {
                    foraDoFiltro++;
                    continue;
                }

                boolean temKeywords = !keywordsNormalizadas.isEmpty();
                Set<String> keywordsMatch = temKeywords
                        ? keywordsNormalizadas.stream()
                                .filter(k -> normalizar(detalhe.titulo()).contains(k))
                                .collect(java.util.stream.Collectors.toSet())
                        : Set.of();

                if (temKeywords && keywordsMatch.isEmpty()) {
                    foraDoFiltro++;
                    continue;
                }

                if (videoRepository.existsByVideoExternoId(detalhe.videoId())) {
                    duplicados++;
                    continue;
                }

                Set<Tag> tags = temKeywords
                        ? tagResolver.resolver(keywordsMatch.stream().toList())
                        : new HashSet<>();

                Video video = Video.builder()
                        .titulo(detalhe.titulo().length() > 160 ? detalhe.titulo().substring(0, 157) + "..." : detalhe.titulo())
                        .descricao("")
                        .urlEmbed("https://www.youtube.com/watch?v=" + detalhe.videoId())
                        .videoExternoId(detalhe.videoId())
                        .plataforma(Plataforma.YOUTUBE)
                        .thumbnailUrl(detalhe.thumbnailUrl())
                        .duracaoSegundos(detalhe.duracaoSegundos())
                        .esShort(true)
                        .categoriaId(null)
                        .criadoPor(adminId)
                        .status(StatusVideo.APROVADO)
                        .tags(tags)
                        .build();
                videoRepository.save(video);
                importados++;
            }
        }

        return new ShortsGarimpoResult(importados, duplicados, foraDoFiltro);
    }

    private static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        return Normalizer.normalize(texto.toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }
}