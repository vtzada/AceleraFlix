package vitortheof.com.br.aceleraflix.video.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vitortheof.com.br.aceleraflix.category.application.CategoriaLookupService;
import vitortheof.com.br.aceleraflix.shared.utils.PermissionChecker;
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

    private final VideoRepository videoRepository;
    private final CategoriaLookupService categoriaLookupService;
    private final YoutubeClient youtubeClient;
    private final TagResolver tagResolver;
    private final VideoMapper videoMapper;
    private final PermissionChecker permissionChecker;

    @Transactional
    public VideoResponse create(VideoRequest request, UUID criadoPor) {
        String videoId = YoutubeUrlParser.extrairVideoId(request.urlYoutube())
                .orElseThrow(() -> new UrlInvalidException("URL do YouTube inválida: " + request.urlYoutube()));
        YoutubeVideoMetadata metadata = youtubeClient.buscarMetadata(videoId)
                .orElseThrow(() -> new UrlInvalidException("Video do YouTube não encontrado ou indisponível."));

        Integer duracaoSegundos = metadata.duracaoSegundos();

        if (!categoriaLookupService.existsById(request.categoriaId())) {
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
                .categoriaId(request.categoriaId())
                .criadoPor(criadoPor)
                .status(StatusVideo.APROVADO)
                .tags(tags)
                .build();
        videoRepository.save(video);
        return videoMapper.toResponse(video);
    }

    @Transactional(readOnly = true)
    public Page<VideoResponse> findAll(Pageable pageable) {
        Page<Video> page = videoRepository.findAll(pageable);
        List<VideoResponse> content = videoMapper.toResponseList(page.getContent());
        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    @Transactional(readOnly = true)
    public Page<VideoResponse> findByCategory(UUID categoriaId, Pageable pageable) {
        Page<Video> page = videoRepository.findByCategoriaId(categoriaId, pageable);
        List<VideoResponse> content = videoMapper.toResponseList(page.getContent());
        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    @Transactional(readOnly = true)
    public Page<VideoResponse> findByCreator(UUID criadoPor, Pageable pageable) {
        Page<Video> page = videoRepository.findByCriadoPor(criadoPor, pageable);
        List<VideoResponse> content = videoMapper.toResponseList(page.getContent());
        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    @Transactional(readOnly = true)
    public Page<VideoResponse> search(String q, Pageable pageable) {
        Page<Video> page = videoRepository
                .findByTituloContainingIgnoreCaseOrDescricaoContainingIgnoreCaseOrTags_NomeContainingIgnoreCase(
                        q, q, q, pageable);
        List<VideoResponse> content = videoMapper.toResponseList(page.getContent());
        return new PageImpl<>(content, pageable, page.getTotalElements());
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

        permissionChecker.verificarPerm(video.getCriadoPor(), usuarioId, isAdmin);

        if (request.categoriaId() == null) {
            throw new CategoriaObrigatoriaException();
        }

        if (!categoriaLookupService.existsById(request.categoriaId())) {
            throw new CategoryNonExistentException(request.categoriaId());
        }

        Set<Tag> tags = tagResolver.resolver(request.tags());

        video.setTitulo(request.titulo());
        video.setDescricao(request.descricao());
        video.setCategoriaId(request.categoriaId());
        video.setTags(tags);

        videoRepository.save(video);
        return videoMapper.toResponse(video);
    }

    @Transactional
    public void delete(UUID id, UUID usuarioId, boolean isAdmin) {
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new VideoNotFoundException(id));

        permissionChecker.verificarPerm(video.getCriadoPor(), usuarioId, isAdmin);
        videoRepository.delete(video);
    }
}