package vitortheof.com.br.aceleraflix.video.application.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vitortheof.com.br.aceleraflix.auth.application.UserLookupService;
import vitortheof.com.br.aceleraflix.auth.application.dto.UserDTO;
import vitortheof.com.br.aceleraflix.category.application.CategoriaLookupService;
import vitortheof.com.br.aceleraflix.category.application.dto.CategoriaDTO;
import vitortheof.com.br.aceleraflix.video.application.dto.VideoResponse;
import vitortheof.com.br.aceleraflix.video.domain.Tag;
import vitortheof.com.br.aceleraflix.video.domain.Video;

import java.util.List;

@RequiredArgsConstructor
@Component
public class VideoMapper {

    private final CategoriaLookupService categoriaLookupService;
    private final UserLookupService userLookupService;

    public VideoResponse toResponse(Video video) {
        CategoriaDTO categoria = categoriaLookupService.findById(video.getCategoriaId());
        UserDTO criador = userLookupService.findById(video.getCriadoPor());
        List<String> tags = video.getTags().stream()
                .map(Tag::getNome)
                .sorted()
                .toList();

        return new VideoResponse(
                video.getId(),
                video.getTitulo(),
                video.getDescricao(),
                video.getUrlEmbed(),
                video.getThumbnailUrl(),
                categoria,
                criador,
                video.getStatus(),
                tags,
                video.getCriadoEm()
        );
    }

}
