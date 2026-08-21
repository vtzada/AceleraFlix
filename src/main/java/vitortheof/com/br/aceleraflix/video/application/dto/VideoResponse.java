package vitortheof.com.br.aceleraflix.video.application.dto;

import vitortheof.com.br.aceleraflix.auth.application.dto.UserDTO;
import vitortheof.com.br.aceleraflix.category.application.dto.CategoriaDTO;
import vitortheof.com.br.aceleraflix.video.domain.StatusVideo;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record VideoResponse(
        UUID id,
        String titulo,
        String descricao,
        String urlEmbed,
        String thumbnailUrl,
        Integer duracaoSegundos,
        CategoriaDTO categoria,
        UserDTO criador,
        StatusVideo status,
        List<String> tags,
        Instant criadoEm) {
}
