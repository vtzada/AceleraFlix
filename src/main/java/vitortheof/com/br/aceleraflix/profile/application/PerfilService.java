package vitortheof.com.br.aceleraflix.profile.application;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vitortheof.com.br.aceleraflix.profile.application.dto.PerfilRequest;
import vitortheof.com.br.aceleraflix.profile.application.dto.PerfilResponse;
import vitortheof.com.br.aceleraflix.profile.application.exception.ProfileAlreadyExistsException;
import vitortheof.com.br.aceleraflix.profile.application.exception.ProfileNotFoundException;
import vitortheof.com.br.aceleraflix.profile.application.exception.UsernameAlreadyExistsException;
import vitortheof.com.br.aceleraflix.profile.application.mapper.PerfilMapper;
import vitortheof.com.br.aceleraflix.profile.domain.Perfil;
import vitortheof.com.br.aceleraflix.profile.infrastructure.PerfilRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PerfilService {

    private final PerfilRepository perfilRepository;
    private final PerfilMapper perfilMapper;

    @Transactional
    public PerfilResponse criarPerfil(UUID usuarioId, PerfilRequest request) {

        if (perfilRepository.findByUsuarioId(usuarioId).isPresent()) {
            throw new ProfileAlreadyExistsException("Já existe um perfil criado.");
        }

        if (perfilRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyExistsException("Este username está sendo usado.");
        }

        Perfil perfil = Perfil.builder()
                .usuarioId(usuarioId)
                .username(request.username())
                .bio(request.bio())
                .githubUrl(request.githubUrl())
                .linkedinUrl(request.linkedinUrl())
                .avatarUrl(resolverAvatarUrl(request.avatarUrl(), usuarioId))
                .verificado(false)
                .build();

        Perfil perfilSalvo;
        try {
            perfilSalvo = perfilRepository.save(perfil);
        } catch (DataIntegrityViolationException ex) {
            if (perfilRepository.existsByUsername(request.username())) {
                throw new UsernameAlreadyExistsException("Este username está sendo usado.");
            }
            throw new ProfileAlreadyExistsException("Já existe um perfil criado.");
        }

        return perfilMapper.toResponse(perfilSalvo);
    }

    @Transactional
    public PerfilResponse atualizarPerfil(UUID usuarioId, PerfilRequest request) {

        Perfil perfil = perfilRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ProfileNotFoundException("Você ainda não criou um perfil."));

        boolean usernameAlterado = !perfil.getUsername().equals(request.username());

        if (usernameAlterado && perfilRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyExistsException("Este username está sendo usado.");
        }

        perfil.setUsername(request.username());
        perfil.setBio(request.bio());
        perfil.setGithubUrl(request.githubUrl());
        perfil.setLinkedinUrl(request.linkedinUrl());
        perfil.setAvatarUrl(resolverAvatarUrl(request.avatarUrl(), usuarioId));

        Perfil perfilAtualizado;
        try {
            perfilAtualizado = perfilRepository.save(perfil);
        } catch (DataIntegrityViolationException ex) {
            throw new UsernameAlreadyExistsException("Este username está sendo usado.");
        }

        return perfilMapper.toResponse(perfilAtualizado);
    }

    @Transactional(readOnly = true)
    public PerfilResponse buscarPerfil(UUID usuarioId) {
        Perfil perfil = perfilRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ProfileNotFoundException("Você ainda não criou um perfil."));

        return perfilMapper.toResponse(perfil);
    }

    @Transactional(readOnly = true)
    public PerfilResponse buscarPorUsername(String username) {
        Perfil perfil = perfilRepository.findByUsername(username.toLowerCase())
                .orElseThrow(() -> new ProfileNotFoundException("Este perfil não foi encontrado."));

        return perfilMapper.toResponse(perfil);
    }

    @Transactional(readOnly = true)
    public PerfilResponse buscarMeuPerfil(UUID usuarioId) {
        Perfil perfil = perfilRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ProfileNotFoundException("Você ainda não criou um perfil."));

        return perfilMapper.toResponse(perfil);
    }

    private String resolverAvatarUrl(String avatarUrlInformado, UUID usuarioId) {
        return avatarUrlInformado != null && !avatarUrlInformado.isBlank()
                ? avatarUrlInformado
                : gerarAvatarPadrao(usuarioId);
    }

    private String gerarAvatarPadrao(UUID usuarioId) {
        return "https://api.dicebear.com/9.x/avataaars/svg?seed=" + usuarioId;
    }
}