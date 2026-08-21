package vitortheof.com.br.aceleraflix.category.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vitortheof.com.br.aceleraflix.category.application.dto.CategoriaDTO;
import vitortheof.com.br.aceleraflix.category.application.exception.CategoryNotFoundException;
import vitortheof.com.br.aceleraflix.category.application.mapper.CategoriaMapper;
import vitortheof.com.br.aceleraflix.category.domain.Categoria;
import vitortheof.com.br.aceleraflix.category.infrastructure.CategoriaRepository;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

    public Map<UUID, CategoriaDTO> findAllById(Set<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        return categoriaRepository.findAllById(ids).stream()
                .map(categoriaMapper::toDTO)
                .collect(Collectors.toMap(CategoriaDTO::id, dto -> dto));
    }

    @Override
    public boolean existsById(UUID id) {
        return categoriaRepository.existsById(id);
    }
}
