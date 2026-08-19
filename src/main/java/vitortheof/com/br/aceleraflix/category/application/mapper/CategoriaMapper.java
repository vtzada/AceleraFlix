package vitortheof.com.br.aceleraflix.category.application.mapper;

import org.springframework.stereotype.Component;
import vitortheof.com.br.aceleraflix.category.application.dto.CategoriaDTO;
import vitortheof.com.br.aceleraflix.category.application.dto.CategoriaRequest;
import vitortheof.com.br.aceleraflix.category.application.dto.CategoriaResponse;
import vitortheof.com.br.aceleraflix.category.domain.Categoria;

@Component
public class CategoriaMapper {

    public CategoriaResponse toResponse(Categoria categoria) {
        return new CategoriaResponse(
                categoria.getId(),
                categoria.getNome(),
                categoria.getSlug(),
                categoria.getDescricao(),
                categoria.getCriadoEm()
        );
    }
    public CategoriaDTO toDTO(Categoria categoria) {
        return new CategoriaDTO(
                categoria.getId(),
                categoria.getNome(),
                categoria.getSlug()
        );
    }
}
