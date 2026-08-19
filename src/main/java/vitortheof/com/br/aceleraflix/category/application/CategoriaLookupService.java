package vitortheof.com.br.aceleraflix.category.application;

import vitortheof.com.br.aceleraflix.category.application.dto.CategoriaDTO;

import java.util.UUID;

public interface CategoriaLookupService {
    CategoriaDTO findById(UUID id);
    boolean existsById(UUID id);
}
