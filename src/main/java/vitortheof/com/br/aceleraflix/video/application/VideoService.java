package vitortheof.com.br.aceleraflix.video.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vitortheof.com.br.aceleraflix.category.application.CategoriaLookupService;
import vitortheof.com.br.aceleraflix.shared.exception.AccessDeniedException;
import vitortheof.com.br.aceleraflix.video.application.dto.VideoRequest;
import vitortheof.com.br.aceleraflix.video.application.dto.VideoResponse;
import vitortheof.com.br.aceleraflix.video.application.dto.VideoUpdateRequest;
import vitortheof.com.br.aceleraflix.video.application.exception.CategoriaObrigatoriaException;
import vitortheof.com.br.aceleraflix.video.application.exception.CategoryNonExistentException;
import vitortheof.com.br.aceleraflix.video.application.exception.UrlInvalidException;
import vitortheof.com.br.aceleraflix.video.application.exception.VideoNotFoundException;
import vitortheof.com.br.aceleraflix.video.application.mapper.VideoMapper;
import vitortheof.com.br.aceleraflix.video.domain.Plataforma;
import vitortheof.com.br.aceleraflix.video.domain.StatusVideo;
import vitortheof.com.br.aceleraflix.video.domain.Tag;
import vitortheof.com.br.aceleraflix.video.domain.Video;
import vitortheof.com.br.aceleraflix.video.infrastructure.VideoRepository;
import vitortheof.com.br.aceleraflix.video.infrastructure.youtube.YoutubeClient;
import vitortheof.com.br.aceleraflix.video.infrastructure.youtube.YoutubeUrlParser;
import vitortheof.com.br.aceleraflix.video.infrastructure.youtube.YoutubeVideoMetadata;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class VideoService {

    private static final int MAX_SHORT_SEGUNDOS = 180;

    private final VideoRepository videoRepository;
    private final CategoriaLookupService categoriaLookupService;
    private final YoutubeClient youtubeClient;
    private final TagResolver tagResolver;
    private final VideoMapper videoMapper;

    public VideoResponse create(VideoRequest request, UUID criadoPor) {
        String videoId = YoutubeUrlParser.extrairVideoId(request.urlYoutube())
                .orElseThrow(() -> new UrlInvalidException("URL do YouTube inválida:" + request.urlYoutube()));
YoutubeVideoMetadata metadata = youtubeClient.buscarMetadata(videoId)
                .orElseThrow(() -> new UrlInvalidException("VA-deo do YouTube nA�o encontrada ou indisponA-vel"));

        Integer duracaoSegundos = metadata.duracaoSegundos();
        boolean esShort = isShort(request.urlYoutube(), duracaoSegundos);

        if (request.categoriaId() == null && !esShort) {
            throw new CategoriaObrigatoriaException();
        }

        if (request.categoriaId() != null && !categoriaLookupService.existsById(request.categoriaId())) {
            throw new CategoryNonExistentException(request.categoriaId());
        }

        Set<Tag> tags = tagResolver.resolver(request.tags());

        Video video = Video.builder()
                .titulo(request.titulo())
                .descricao(request.descricao())
                .urlEmbed(request.urlYoutube())
                .videoExternoId(videoId)
                .plataforma(Plataforma.YOUTUBE)
                .thumbnailUrl(metadata.thumbnailUrl())
                .duracaoSegundos(duracaoSegundos)
                .esShort(esShort)
                .categoriaId(request.categoriaId())
                .criadoPor(criadoPor)
                .status(StatusVideo.APROVADO)
                .tags(tags)
                .build();
        videoRepository.save(video);
        return videoMapper.toResponse(video);
    }

    private boolean isShort(String urlYoutube, Integer duracaoSegundos) {
        boolean urlDeShort = urlYoutube != null && urlYoutube.contains("/shorts/");
        boolean curto = duracaoSegundos != null && duracaoSegundos <= MAX_SHORT_SEGUNDOS;
        return urlDeShort || curto;
    }

    @Transactional(readOnly = true)
    public Page<VideoResponse> findAll(Pageable pageable) {
        return videoRepository.findAll(pageable).map(videoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<VideoResponse> findByCategory(UUID categoriaId, Pageable pageable) {
        return videoRepository.findByCategoriaId(categoriaId, pageable).map(videoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<VideoResponse> findByCreator(UUID criadoPor, Pageable pageable) {
        return videoRepository.findByCriadoPor(criadoPor, pageable).map(videoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<VideoResponse> search(String q, Pageable pageable) {
        return videoRepository
                .findByTituloContainingIgnoreCaseOrDescricaoContainingIgnoreCaseOrTags_NomeContainingIgnoreCase(
                        q, q, q, pageable)
                .map(videoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<VideoResponse> findShorts(Pageable pageable) {
        return videoRepository.findByEsShortTrue(pageable).map(videoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public VideoResponse findById(UUID id) {
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new VideoNotFoundException(id));
        return videoMapper.toResponse(video);
    }

    @Transactional
    public VideoResponse update(UUID id, VideoUpdateRequest request, UUID usuarioId, boolean isAdmin) {
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new VideoNotFoundException(id));

        verificarPerm(video.getCriadoPor(), usuarioId, isAdmin);

        boolean esShort = video.isEsShort();
        if (request.categoriaId() == null && !esShort) {
            throw new CategoriaObrigatoriaException();
        }

        if (request.categoriaId() != null && !categoriaLookupService.existsById(request.categoriaId())) {
            throw new CategoryNonExistentException(request.categoriaId());
        }

        Set<Tag> tags = tagResolver.resolver(request.tags());

        video.setTitulo(request.titulo());
        video.setDescricao(request.descricao());
        video.setCategoriaId(esShort ? null : request.categoriaId());
        video.setTags(tags);

        videoRepository.save(video);

        return videoMapper.toResponse(video);
    }

    @Transactional
    public void delete(UUID id, UUID usuarioId, boolean isAdmin) {
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new VideoNotFoundException(id));

        verificarPerm(video.getCriadoPor(), usuarioId, isAdmin);

        videoRepository.delete(video);
    }

    private void verificarPerm(UUID criadoPor, UUID usuarioId, boolean isAdmin) {
        if (!isAdmin && !criadoPor.equals(usuarioId)) {
            throw new AccessDeniedException("Você não tem permissão para modificar este vídeo");
        }
    }

}
