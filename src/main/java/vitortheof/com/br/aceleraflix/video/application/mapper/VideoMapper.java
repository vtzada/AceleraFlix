package vitortheof.com.br.aceleraflix.video.application.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vitortheof.com.br.aceleraflix.auth.application.UserLookupService;
import vitortheof.com.br.aceleraflix.auth.application.dto.UserDTO;
import vitortheof.com.br.aceleraflix.category.application.CategoriaLookupService;
import vitortheof.com.br.aceleraflix.category.application.dto.CategoriaDTO;
import vitortheof.com.br.aceleraflix.profile.application.PerfilLookupService;
import vitortheof.com.br.aceleraflix.profile.domain.Perfil;
import vitortheof.com.br.aceleraflix.video.application.dto.CriadorDTO;
import vitortheof.com.br.aceleraflix.video.application.dto.VideoResponse;
import vitortheof.com.br.aceleraflix.video.domain.Tag;
import vitortheof.com.br.aceleraflix.video.domain.Video;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class VideoMapper {

    private final CategoriaLookupService categoriaLookupService;
    private final UserLookupService userLookupService;
    private final PerfilLookupService perfilLookupService;

    public VideoResponse toResponse(Video video) {
        CategoriaDTO categoria = video.getCategoriaId() != null
                ? categoriaLookupService.findById(video.getCategoriaId())
                : null;

        UserDTO usuario = userLookupService.findById(video.getCriadoPor());
        Perfil perfil = perfilLookupService
                .findByUsuarioIds(Set.of(video.getCriadoPor()))
                .get(video.getCriadoPor());
        CriadorDTO criador = buildCriador(usuario, perfil);

        return buildResponse(video, categoria, criador);
    }

    public List<VideoResponse> toResponseList(List<Video> videos) {
        if (videos == null || videos.isEmpty()) {
            return List.of();
        }

        Set<UUID> categoriaIds = videos.stream()
                .map(Video::getCategoriaId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<UUID> criadorIds = videos.stream()
                .map(Video::getCriadoPor)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<UUID, CategoriaDTO> categoriasMap = categoriaLookupService.findAllById(categoriaIds);
        Map<UUID, UserDTO> usuariosMap = userLookupService.findAllById(criadorIds);
        Map<UUID, Perfil> perfisMap = perfilLookupService.findByUsuarioIds(criadorIds);

        return videos.stream().map(video -> {
            CategoriaDTO categoria = video.getCategoriaId() != null
                    ? categoriasMap.get(video.getCategoriaId())
                    : null;

            CriadorDTO criador = buildCriador(
                    usuariosMap.get(video.getCriadoPor()),
                    perfisMap.get(video.getCriadoPor()));

            return buildResponse(video, categoria, criador);
        }).toList();
    }

    private CriadorDTO buildCriador(UserDTO usuario, Perfil perfil) {
        if (usuario == null) {
            return null;
        }
        return new CriadorDTO(
                usuario.id(),
                usuario.nome(),
                perfil != null ? perfil.getUsername() : null,
                perfil != null ? perfil.getAvatarUrl() : null);
    }

    private VideoResponse buildResponse(Video video, CategoriaDTO categoria, CriadorDTO criador) {
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
                video.getDuracaoSegundos(),
                categoria,
                criador,
                video.getStatus(),
                tags,
                video.getCriadoEm()
        );
    }
}
