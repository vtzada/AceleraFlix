package vitortheof.com.br.aceleraflix.shared.exception;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import vitortheof.com.br.aceleraflix.auth.application.exception.CredentialsInvalidException;
import vitortheof.com.br.aceleraflix.auth.application.exception.EmailAlreadyExistsException;
import vitortheof.com.br.aceleraflix.auth.application.exception.SolicitationInvalidException;
import vitortheof.com.br.aceleraflix.auth.application.exception.UserNotFoundException;
import vitortheof.com.br.aceleraflix.auth.infrastructure.security.TokenInvalidoException;
import vitortheof.com.br.aceleraflix.category.application.exception.CategoryAlreadyExistsException;
import vitortheof.com.br.aceleraflix.category.application.exception.CategoryNotFoundException;
import vitortheof.com.br.aceleraflix.category.application.exception.CategoryWithVideoException;
import vitortheof.com.br.aceleraflix.profile.application.exception.ProfileAlreadyExistsException;
import vitortheof.com.br.aceleraflix.profile.application.exception.ProfileNotFoundException;
import vitortheof.com.br.aceleraflix.profile.application.exception.UsernameAlreadyExistsException;
import vitortheof.com.br.aceleraflix.roadmap.application.exceptions.RoadmapAlreadyExistsException;
import vitortheof.com.br.aceleraflix.roadmap.application.exceptions.RoadmapNotFoundException;
import vitortheof.com.br.aceleraflix.video.application.exception.CategoriaObrigatoriaException;
import vitortheof.com.br.aceleraflix.video.application.exception.CategoryNonExistentException;
import vitortheof.com.br.aceleraflix.video.application.exception.UrlInvalidException;
import vitortheof.com.br.aceleraflix.video.application.exception.VideoNotFoundException;
import vitortheof.com.br.aceleraflix.video.infrastructure.youtube.YoutubeApiException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Nome que o Postgres dá ao UNIQUE de roadmaps.slug (V11).
    private static final String ROADMAP_SLUG_UNIQUE = "roadmaps_slug_key";

    @ExceptionHandler(TokenInvalidoException.class)
    public ResponseEntity<ErroResponse> handleTokenInvalido(TokenInvalidoException ex) {
        return construirResposta(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErroResponse> handleEmailJaCadastrado(EmailAlreadyExistsException ex) {
        return construirResposta(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(CredentialsInvalidException.class)
    public ResponseEntity<ErroResponse> handleCredenciaisInvalidas(CredentialsInvalidException ex) {
        return construirResposta(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(ForbiddenOperationException.class)
    public ResponseEntity<ErroResponse> handleAcessoNegado(ForbiddenOperationException ex) {
        return construirResposta(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(SolicitationInvalidException.class)
    public ResponseEntity<ErroResponse> handleSolicitacaoInvalida(SolicitationInvalidException ex) {
        return construirResposta(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidacao(MethodArgumentNotValidException ex) {
        Map<String, String> erros = new HashMap<>();
        for (FieldError erro : ex.getBindingResult().getFieldErrors()) {
            erros.put(erro.getField(), erro.getDefaultMessage());
        }
        return ResponseEntity.badRequest().body(erros);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> handleCorpoInvalido(HttpMessageNotReadableException ex) {
        return construirResposta(HttpStatus.BAD_REQUEST, "Corpo da requisição ausente ou inválido.");
    }

    @ExceptionHandler(CategoryAlreadyExistsException.class)
    public ResponseEntity<ErroResponse> handleCategoriaJaExiste(CategoryAlreadyExistsException ex) {
        return construirResposta(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ErroResponse> handleCategoriaNaoEncontrada(CategoryNotFoundException ex) {
        return construirResposta(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(CategoryNonExistentException.class)
    public ResponseEntity<ErroResponse> handleCategoriaInexistente(CategoryNonExistentException ex) {
        return construirResposta(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(CategoryWithVideoException.class)
    public ResponseEntity<ErroResponse> handleCategoriaComVideos(CategoryWithVideoException ex) {
        return construirResposta(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(UrlInvalidException.class)
    public ResponseEntity<ErroResponse> handleUrlInvalida(UrlInvalidException ex) {
        return construirResposta(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(VideoNotFoundException.class)
    public ResponseEntity<ErroResponse> handleVideoNaoEncontrado(VideoNotFoundException ex) {
        return construirResposta(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(CategoriaObrigatoriaException.class)
    public ResponseEntity<ErroResponse> handleCategoriaObrigatoria(CategoriaObrigatoriaException ex) {
        return construirResposta(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(YoutubeApiException.class)
    public ResponseEntity<ErroResponse> handleYoutubeApi(YoutubeApiException ex) {
        return construirResposta(HttpStatus.BAD_GATEWAY, "Não foi possível validar o vídeo no momento. Tente novamente.");
    }

    @ExceptionHandler(ProfileAlreadyExistsException.class)
    public ResponseEntity<ErroResponse> handlePerfilJaExiste(ProfileAlreadyExistsException ex) {
        return construirResposta(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<ErroResponse> handleUsernameJaUsado(UsernameAlreadyExistsException ex) {
        return construirResposta(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(ProfileNotFoundException.class)
    public ResponseEntity<ErroResponse> handlePerfilNaoEncontrado(ProfileNotFoundException ex) {
        return construirResposta(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(RoadmapNotFoundException.class)
    public ResponseEntity<ErroResponse> handleRoadmapNaoEncontrado(RoadmapNotFoundException ex) {
        return construirResposta(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(RoadmapAlreadyExistsException.class)
    public ResponseEntity<ErroResponse> handleRoadmapJaExiste(RoadmapAlreadyExistsException ex) {
        return construirResposta(HttpStatus.CONFLICT, ex.getMessage());
    }

    // A consulta prévia do RoadmapService não cobre duas gravações simultâneas do mesmo slug: nesse caso quem
    // barra é a restrição do banco (no flush ou só no commit). Apenas essa restrição vira 409; qualquer outra
    // violação de integridade continua sendo erro interno.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResponse> handleViolacaoDeIntegridade(DataIntegrityViolationException ex) {
        if (violaRestricao(ex, ROADMAP_SLUG_UNIQUE)) {
            return construirResposta(HttpStatus.CONFLICT, "Já existe um roadmap com este slug");
        }
        return handleErrosNaoTratados(ex);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErroResponse> handleUsuarioNaoEncontrado(UserNotFoundException ex) {
        return construirResposta(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> handleErrosNaoTratados(Exception ex) {
        log.error("Erro interno não tratado capturado pelo fallback: ", ex);
        return construirResposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno no servidor. Tente novamente mais tarde.");
    }

    private boolean violaRestricao(Throwable ex, String restricao) {
        for (Throwable causa = ex; causa != null; causa = causa.getCause()) {
            if (causa instanceof ConstraintViolationException violacao
                    && restricao.equalsIgnoreCase(violacao.getConstraintName())) {
                return true;
            }
        }
        return false;
    }

    private ResponseEntity<ErroResponse> construirResposta(HttpStatus status, String mensagem) {
        ErroResponse erro = new ErroResponse(status.value(), mensagem, Instant.now());
        return ResponseEntity.status(status).body(erro);
    }

    public record ErroResponse(int status, String mensagem, Instant timestamp) {}
}