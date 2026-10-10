package vitortheof.com.br.aceleraflix.auth.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import vitortheof.com.br.aceleraflix.auth.infrastructure.RefreshTokenRepository;

import java.util.UUID;

/**
 * Operações de revogação em transação própria (REQUIRES_NEW).
 * Necessário porque o fluxo de refresh lança exceção após detectar reuso/expiração,
 * e a exceção derrubaria o rollback da transação principal — perdendo a revogação.
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenRevogacao {

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void revogarTodosDoUsuario(UUID userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void marcarRevogado(UUID tokenId) {
        refreshTokenRepository.findById(tokenId).ifPresent(token -> token.setRevoked(true));
    }
}
