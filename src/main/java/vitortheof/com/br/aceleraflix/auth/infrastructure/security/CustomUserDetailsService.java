package vitortheof.com.br.aceleraflix.auth.infrastructure.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import vitortheof.com.br.aceleraflix.auth.infrastructure.UsuarioRepository;

import java.util.UUID;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        return usuarioRepository.findByEmail(email)
                .map(UserAuth::new)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario nao encontrado"));
    }

    public UserDetails loadUserById(UUID id) {
        return usuarioRepository.findById(id)
                .map(UserAuth::new)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + id));
    }
}
