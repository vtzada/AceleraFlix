package vitortheof.com.br.aceleraflix.category.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vitortheof.com.br.aceleraflix.category.application.dto.CategoriaDTO;
import vitortheof.com.br.aceleraflix.category.application.exception.CategoryNotFoundException;
import vitortheof.com.br.aceleraflix.category.application.mapper.CategoriaMapper;
import vitortheof.com.br.aceleraflix.category.domain.Categoria;
import vitortheof.com.br.aceleraflix.category.infrastructure.CategoriaRepository;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class CategoriaLookupServiceImpl implements CategoriaLookupService {

    private final CategoriaRepository categoriaRepository;
    private final CategoriaMapper categoriaMapper;

    @Override
    public CategoriaDTO findById(UUID id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
        return categoriaMapper.toDTO(categoria);
    }

    @Override
    public boolean existsById(UUID id) {
        return categoriaRepository.existsById(id);
    }
}
