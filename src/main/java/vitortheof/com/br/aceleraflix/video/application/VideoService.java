package vitortheof.com.br.aceleraflix.video.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vitortheof.com.br.aceleraflix.category.application.CategoriaLookupService;
import vitortheof.com.br.aceleraflix.video.application.dto.VideoRequest;
import vitortheof.com.br.aceleraflix.video.application.dto.VideoResponse;
import vitortheof.com.br.aceleraflix.video.application.exception.CategoryCategoryNonExistentException;
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

    public VideoResponse create(VideoRequest request, UUID criadoPor) {
        String videoId = YoutubeUrlParser.extrairVideoId(request.urlYoutube())
                .orElseThrow(() -> new UrlInvalidException("URL do YouTube inválida:" + request.urlYoutube()));
        YoutubeVideoMetadata metadata = youtubeClient.buscarMetadata(videoId)
                .orElseThrow(() -> new UrlInvalidException("Vídeo do YouTube não encontrada ou indisponível"));

        if (!categoriaLookupService.existsById(request.categoriaId())) {
            throw new CategoryCategoryNonExistentException(request.categoriaId());
        }

        Set<Tag> tags = tagResolver.resolver(request.tags());

        Video video = Video.builder()
                .titulo(request.titulo())
                .descricao(request.descricao())
                .urlEmbed(request.urlYoutube())
                .videoExternoId(videoId)
                .plataforma(Plataforma.YOUTUBE)
                .thumbnailUrl(metadata.thumbnailUrl())
                .categoriaId(request.categoriaId())
                .criadoPor(criadoPor)
                .status(StatusVideo.APROVADO)
                .tags(tags)
                .build();
        videoRepository.save(video);
        return videoMapper.toResponse(video);
    }

    @Transactional(readOnly = true)
    public List<VideoResponse> findAll() {
        return videoRepository.findAll().stream().map(videoMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<VideoResponse> findByCategory(UUID categoriaId) {
        return videoRepository.findByCategoriaId(categoriaId).stream().map(videoMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public VideoResponse findById(UUID id) {
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new VideoNotFoundException(id));
        return videoMapper.toResponse(video);
    }
}
