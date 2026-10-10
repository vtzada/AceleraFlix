package vitortheof.com.br.aceleraflix.auth.infrastructure.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import vitortheof.com.br.aceleraflix.auth.domain.Usuario;

import java.time.Instant;
import java.util.UUID;

@Component
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-access}")
    private long expirationAcess;

    @Value("${jwt.expiration-refresh}")
    private long expirationRefresh;

    private static final String ISSUER = "aceleraflix";

    public String generateAccessToken(Usuario usuario) {
       return JWT.create()
               .withIssuer(ISSUER)
               .withSubject(usuario.getId().toString())
               .withClaim("type", "access")
               .withClaim("role", usuario.getRole().name())
               .withClaim("email", usuario.getEmail())
               .withIssuedAt(Instant.now())
               .withExpiresAt(Instant.now().plusMillis(expirationAcess))
               .sign(Algorithm.HMAC256(secret));
    }

    public String generateRefreshToken(Usuario usuario) {
        return generateRefreshToken(usuario, UUID.randomUUID());
    }

    public String generateRefreshToken(Usuario usuario, UUID jti) {
        return JWT.create()
                .withIssuer(ISSUER)
                .withSubject(usuario.getId().toString())
                .withClaim("type", "refresh")
                .withClaim("jti", jti.toString())
                .withIssuedAt(Instant.now())
                .withExpiresAt(Instant.now().plusMillis(expirationRefresh))
                .sign(Algorithm.HMAC256(secret));
    }

    public RefreshData validarRefreshToken(String token) {
        try {
            DecodedJWT decoded = JWT.require(Algorithm.HMAC256(secret))
                    .withIssuer(ISSUER)
                    .withClaim("type", "refresh")
                    .build()
                    .verify(token);
            UUID userId = UUID.fromString(decoded.getSubject());
            String jtiClaim = decoded.getClaim("jti").asString();
            if (jtiClaim == null) {
                throw new TokenInvalidoException("Refresh token sem identificador");
            }
            return new RefreshData(userId, UUID.fromString(jtiClaim), decoded.getExpiresAtAsInstant());
        } catch (JWTVerificationException | IllegalArgumentException e) {
            throw new TokenInvalidoException("Token inválido ou expirado");
        }
    }

    public record RefreshData(UUID userId, UUID jti, Instant expiresAt) {}

    public UUID validarTokenERetornarUsuarioId(String token, String expectedType) {
        try {
            DecodedJWT decoded = JWT.require(Algorithm.HMAC256(secret))
                    .withIssuer(ISSUER)
                    .withClaim("type", expectedType)
                    .build()
                    .verify(token);
            return UUID.fromString(decoded.getSubject());
        } catch (JWTVerificationException e) {
            throw new TokenInvalidoException("Token inválido ou expirado");
        }
    }
}
