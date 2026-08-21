package vitortheof.com.br.aceleraflix.auth.application.mapper;

import org.springframework.stereotype.Component;
import vitortheof.com.br.aceleraflix.auth.application.dto.UserDTO;
import vitortheof.com.br.aceleraflix.auth.domain.Usuario;

@Component
public class UserMapper {

    public UserDTO toDTO(Usuario user) {
        return new UserDTO(
                user.getId(),
                user.getNome(),
                user.getEmail(),
                user.getRole());
    }
}
