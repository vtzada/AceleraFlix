package vitortheof.com.br.aceleraflix.auth.application;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vitortheof.com.br.aceleraflix.auth.application.dto.LoginRequest;
import vitortheof.com.br.aceleraflix.auth.application.dto.RegisterRequest;
import vitortheof.com.br.aceleraflix.auth.application.dto.TokenResponse;
import vitortheof.com.br.aceleraflix.auth.application.exception.CredentialsInvalidException;
import vitortheof.com.br.aceleraflix.auth.application.exception.EmailAlreadyExistsException;
import vitortheof.com.br.aceleraflix.auth.domain.RefreshToken;
import vitortheof.com.br.aceleraflix.auth.domain.RoleUsuario;
import vitortheof.com.br.aceleraflix.auth.domain.StatusEditor;
import vitortheof.com.br.aceleraflix.auth.domain.Usuario;
import vitortheof.com.br.aceleraflix.auth.infrastructure.RefreshTokenRepository;
import vitortheof.com.br.aceleraflix.auth.infrastructure.UsuarioRepository;
import vitortheof.com.br.aceleraflix.auth.infrastructure.security.JwtProvider;
import vitortheof.com.br.aceleraflix.auth.infrastructure.security.TokenInvalidoException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenRevogacao refreshTokenRevogacao;
    private final JwtProvider jwtProvider;

    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Value("${jwt.expiration-refresh}")
    private long expirationRefresh;

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
        return emitirParTokens(usuario);
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(CredentialsInvalidException::new);

        if (!passwordEncoder.matches(request.senha(), usuario.getPassword())) {
            throw new CredentialsInvalidException();
        }
        // Limpeza oportunista de tokens expirados do usuário seria ideal com job;
        // aqui apenas emite novo par (tokens expirados são rejeitados no refresh).
        return emitirParTokens(usuario);
    }

    @Transactional
    public TokenResponse refreshToken(String refreshToken) {
        JwtProvider.RefreshData data = jwtProvider.validarRefreshToken(refreshToken);
        String hash = sha256Hex(refreshToken);

        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new TokenInvalidoException("Refresh token inválido ou expirado"));

        // Reuso de token já rotacionado = possível roubo: invalida toda a família
        // em transação própria (a exceção abaixo faria rollback da atual).
        if (stored.isRevoked()) {
            refreshTokenRevogacao.revogarTodosDoUsuario(stored.getUserId());
            throw new TokenInvalidoException("Refresh token reutilizado. Faça login novamente.");
        }

        if (!stored.getUserId().equals(data.userId()) || !stored.getJti().equals(data.jti())) {
            throw new TokenInvalidoException("Refresh token inválido");
        }

        if (stored.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenRevogacao.marcarRevogado(stored.getId());
            throw new TokenInvalidoException("Refresh token expirado");
        }

        Usuario usuario = usuarioRepository.findById(data.userId())
                .orElseThrow(CredentialsInvalidException::new);

        // Rotação: invalida o antigo e emite novo par.
        UUID novoJti = UUID.randomUUID();
        stored.setRevoked(true);
        stored.setReplacedByJti(novoJti);
        refreshTokenRepository.save(stored);

        return emitirParTokens(usuario, novoJti);
    }

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }
        String hash = sha256Hex(refreshToken);
        refreshTokenRepository.findByTokenHash(hash).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    @Transactional
    public void logoutAll(UUID userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    private TokenResponse emitirParTokens(Usuario usuario) {
        return emitirParTokens(usuario, UUID.randomUUID());
    }

    private TokenResponse emitirParTokens(Usuario usuario, UUID jti) {
        String accessToken = jwtProvider.generateAccessToken(usuario);
        String refreshToken = jwtProvider.generateRefreshToken(usuario, jti);
        RefreshToken entity = RefreshToken.builder()
                .userId(usuario.getId())
                .jti(jti)
                .tokenHash(sha256Hex(refreshToken))
                .expiresAt(Instant.now().plusMillis(expirationRefresh))
                .revoked(false)
                .build();
        refreshTokenRepository.save(entity);
        return new TokenResponse(accessToken, refreshToken);
    }

    static String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }
}
