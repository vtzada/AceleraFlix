package vitortheof.com.br.aceleraflix.auth.application;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vitortheof.com.br.aceleraflix.auth.application.dto.LoginRequest;
import vitortheof.com.br.aceleraflix.auth.application.dto.RegisterRequest;
import vitortheof.com.br.aceleraflix.auth.application.dto.TokenResponse;
import vitortheof.com.br.aceleraflix.auth.application.exception.CredentialsInvalidException;
import vitortheof.com.br.aceleraflix.auth.application.exception.EmailAlreadyExistsException;
import vitortheof.com.br.aceleraflix.auth.domain.RoleUsuario;
import vitortheof.com.br.aceleraflix.auth.domain.StatusEditor;
import vitortheof.com.br.aceleraflix.auth.domain.Usuario;
import vitortheof.com.br.aceleraflix.auth.infrastructure.UsuarioRepository;
import vitortheof.com.br.aceleraflix.auth.infrastructure.security.JwtProvider;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public TokenResponse register(RegisterRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        Usuario usuario = Usuario.builder()
                .nome(request.nome())
                .email(request.email())
                .password(passwordEncoder.encode(request.senha()))
                .role(RoleUsuario.USUARIO)
                .statusEditor(StatusEditor.NENHUM)
                .build();

        usuarioRepository.save(usuario);
        return generateToken(usuario);
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(CredentialsInvalidException::new);

        if (!passwordEncoder.matches(request.senha(), usuario.getPassword())) {
            throw new CredentialsInvalidException();
        }
        return generateToken(usuario);
    }

    private TokenResponse generateToken(Usuario usuario) {
        String accessToken = jwtProvider.generateAccessToken(usuario);
        String refreshToken = jwtProvider.generateRefreshToken(usuario);
        return new TokenResponse(accessToken, refreshToken);
    }

    @Transactional(readOnly = true)
    public TokenResponse refreshToken(String refreshToken) {
        UUID usuarioId = jwtProvider.validarTokenERetornarUsuarioId(refreshToken, "refresh");
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(CredentialsInvalidException::new);

        return generateToken(usuario);
    }

}
