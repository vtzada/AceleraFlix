package vitortheof.com.br.aceleraflix.category.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vitortheof.com.br.aceleraflix.category.application.dto.CategoriaRequest;
import vitortheof.com.br.aceleraflix.category.application.dto.CategoriaResponse;
import vitortheof.com.br.aceleraflix.category.application.exception.CategoryAlreadyExistsException;
import vitortheof.com.br.aceleraflix.category.application.exception.CategoryNotFoundException;
import vitortheof.com.br.aceleraflix.category.application.mapper.CategoriaMapper;
import vitortheof.com.br.aceleraflix.category.domain.Categoria;
import vitortheof.com.br.aceleraflix.category.infrastructure.CategoriaRepository;

import java.text.Normalizer;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final CategoriaMapper categoriaMapper;

    @Transactional
    public CategoriaResponse create(CategoriaRequest request, UUID criadoPor) {
        String slug = gerarSlug(request.nome());

        if(categoriaRepository.existsBySlug(slug)) {
            throw new CategoryAlreadyExistsException(request.nome());
        }
        Categoria categoria = Categoria.builder()
                .nome(request.nome())
                .slug(slug)
                .descricao(request.descricao())
                .criadoPor(criadoPor)
                .build();

        categoriaRepository.save(categoria);

        return categoriaMapper.toResponse(categoria);
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> findAll() {
        return categoriaRepository.findAll().stream()
                .map(categoriaMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoriaResponse findById(UUID id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
        return categoriaMapper.toResponse(categoria);
    }

    private String gerarSlug(String slug){
        String semAcento = Normalizer.normalize(slug, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return Pattern.compile("[^a-zA-Z0-9]+")
                .matcher(semAcento.toLowerCase())
                .replaceAll("-")
                .replaceAll("^-|-$", "");
    }
}
