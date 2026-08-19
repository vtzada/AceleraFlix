package vitortheof.com.br.aceleraflix.category.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.Id;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vitortheof.com.br.aceleraflix.auth.domain.Usuario;
import vitortheof.com.br.aceleraflix.auth.infrastructure.security.UserAuth;
import vitortheof.com.br.aceleraflix.category.application.CategoriaService;
import vitortheof.com.br.aceleraflix.category.application.dto.CategoriaRequest;
import vitortheof.com.br.aceleraflix.category.application.dto.CategoriaResponse;
import vitortheof.com.br.aceleraflix.category.application.mapper.CategoriaMapper;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/categorias")
@Tag(name = "Categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    @GetMapping
    public ResponseEntity<List<CategoriaResponse>> findAll() {
        return ResponseEntity.ok(categoriaService.findAll());
    }

    @GetMapping("/id")
    public ResponseEntity<CategoriaResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(categoriaService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('EDITOR') or hasRole('ADMIN')")
    public ResponseEntity<CategoriaResponse> create(@Valid @RequestBody CategoriaRequest request,
                                                    @AuthenticationPrincipal UserAuth userAuth) {
        UUID criadoPor = userAuth.getUsuario().getId();
        CategoriaResponse response = categoriaService.create(request, criadoPor);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
