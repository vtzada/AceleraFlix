package vitortheof.com.br.aceleraflix.auth.infrastructure.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.stereotype.Component;
import vitortheof.com.br.aceleraflix.auth.domain.Usuario;

import java.time.Instant;
import java.util.UUID;

@Component
public class JwtProvider {

    private String secret;

    private long expirationAcess;

    private long expirationRefresh;

    private static final String ISSUER = "aceleraflix";

    public String generateAcessToken(Usuario usuario) {
       return JWT.create()
               .withIssuer(ISSUER)
               .withSubject(usuario.getId().toString())
               .withClaim("role", usuario.getRole().name())
               .withClaim("email", usuario.getEmail())
               .withIssuedAt(Instant.now())
               .withExpiresAt(Instant.now().plusMillis(expirationAcess))
               .sign(Algorithm.HMAC256(secret));
    }

    public String generateRefreshToken(Usuario usuario) {
        return JWT.create()
                .withIssuer(ISSUER)
                .withSubject(usuario.getId().toString())
                .withIssuedAt(Instant.now())
                .withExpiresAt(Instant.now().plusMillis(expirationRefresh))
                .sign(Algorithm.HMAC256(secret));
    }

    public UUID validarTokenERetornarUsuarioId(String token) {
        try {
            DecodedJWT decoded = JWT.require(Algorithm.HMAC256(secret))
                    .withIssuer(ISSUER)
                    .build()
                    .verify(token);
            return UUID.fromString(decoded.getSubject());
        } catch (JWTVerificationException e) {
            throw new TokenInvalidoException("Token inválido ou expirado");
        }
    }
}
