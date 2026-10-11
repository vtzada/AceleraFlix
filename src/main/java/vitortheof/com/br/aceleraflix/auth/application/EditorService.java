package vitortheof.com.br.aceleraflix.auth.application;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vitortheof.com.br.aceleraflix.auth.application.exception.SolicitationInvalidException;
import vitortheof.com.br.aceleraflix.auth.domain.RoleUsuario;
import vitortheof.com.br.aceleraflix.auth.domain.StatusEditor;
import vitortheof.com.br.aceleraflix.auth.domain.Usuario;
import vitortheof.com.br.aceleraflix.auth.infrastructure.UsuarioRepository;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class EditorService {

    private final UsuarioRepository usuarioRepository;

    @Transactional
    public void solicitarEdicao(UUID userId){
        Usuario usuario = buscarUser(userId);

        if (usuario.getRole() == RoleUsuario.ADMIN) {
            throw new SolicitationInvalidException("Administradores já têm permissão de edição e não podem solicitar o cargo de editor");
        }

        if(usuario.getStatusEditor() == StatusEditor.PENDENTE){
            throw new SolicitationInvalidException("Você já tem uma solicitação pendente");
        }
        if (usuario.getStatusEditor() == StatusEditor.APROVADO){
            throw new SolicitationInvalidException("Você já é editor");
        }

        usuario.setStatusEditor(StatusEditor.PENDENTE);
        usuarioRepository.save(usuario);
    }

    public List<Usuario> listarPendentes(){
        return usuarioRepository.findByStatusEditor(StatusEditor.PENDENTE);
    }

    public List<Usuario> listarAprovados(){
        return usuarioRepository.findByStatusEditor(StatusEditor.APROVADO);
    }

    @Transactional
    public void aprovar(UUID usuarioId){
        Usuario usuario = buscarUser(usuarioId);

        if(usuario.getStatusEditor() != StatusEditor.PENDENTE){
            throw new SolicitationInvalidException("Usuário não possui solicitação pendente");
        }

        usuario.setStatusEditor(StatusEditor.APROVADO);
        alterarRoleSeNaoForAdmin(usuario, RoleUsuario.EDITOR);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void rejeitar(UUID usuarioId){
        Usuario usuario = buscarUser(usuarioId);
        if(usuario.getStatusEditor() != StatusEditor.PENDENTE){
            throw new SolicitationInvalidException("Usuário não possui solicitação pendente");
        }
        usuario.setStatusEditor(StatusEditor.NENHUM);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void revogar(UUID usuarioId){
        Usuario usuario = buscarUser(usuarioId);
        if(usuario.getStatusEditor() != StatusEditor.APROVADO){
            throw new SolicitationInvalidException("Usuário não é editor");
        }
        usuario.setStatusEditor(StatusEditor.REVOGADO);
        alterarRoleSeNaoForAdmin(usuario, RoleUsuario.USUARIO);
        usuarioRepository.save(usuario);
    }

    // O cargo de ADMIN nunca é rebaixado pelo fluxo de editor.
    private void alterarRoleSeNaoForAdmin(Usuario usuario, RoleUsuario novoRole) {
        if (usuario.getRole() != RoleUsuario.ADMIN) {
            usuario.setRole(novoRole);
        }
    }

    private Usuario buscarUser(UUID userId){
        return usuarioRepository.findById(userId)
                .orElseThrow(() -> new SolicitationInvalidException("Usuário não encontrado"));
    }

}
