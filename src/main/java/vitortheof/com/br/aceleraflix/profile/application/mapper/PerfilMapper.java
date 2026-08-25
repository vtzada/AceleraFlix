package vitortheof.com.br.aceleraflix.profile.application.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vitortheof.com.br.aceleraflix.auth.application.UserLookupService;
import vitortheof.com.br.aceleraflix.auth.application.dto.UserDTO;
import vitortheof.com.br.aceleraflix.profile.application.dto.PerfilResponse;
import vitortheof.com.br.aceleraflix.profile.domain.Perfil;

@Component
@RequiredArgsConstructor
public class PerfilMapper {

    private final UserLookupService userLookupService;

    public PerfilResponse toResponse(Perfil perfil) {
        UserDTO usuario = userLookupService.findById(perfil.getUsuarioId());

        return new PerfilResponse(
                usuario.nome(),
                perfil.getUsername(),
                perfil.getBio(),
                perfil.getAvatarUrl(),
                perfil.getGithubUrl(),
                perfil.getLinkedinUrl(),
                perfil.isVerificado()
        );
    }
}