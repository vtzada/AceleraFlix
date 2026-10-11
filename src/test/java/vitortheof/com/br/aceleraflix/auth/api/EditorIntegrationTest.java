package vitortheof.com.br.aceleraflix.auth.api;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import vitortheof.com.br.aceleraflix.auth.domain.RoleUsuario;
import vitortheof.com.br.aceleraflix.auth.domain.StatusEditor;
import vitortheof.com.br.aceleraflix.auth.domain.Usuario;
import vitortheof.com.br.aceleraflix.auth.infrastructure.UsuarioRepository;
import vitortheof.com.br.aceleraflix.auth.infrastructure.security.JwtProvider;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Como AvaliacaoVideoIntegrationTest: precisa do Postgres apontado por DB_URL e reverte tudo ao final de cada teste.
@SpringBootTest
@Transactional
class EditorIntegrationTest {

    @Autowired WebApplicationContext context;
    @Autowired JwtProvider jwt;
    @Autowired UsuarioRepository usuarios;
    @Autowired EntityManager entityManager;

    MockMvc mvc;
    String sufixo;
    Usuario admin;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        sufixo = UUID.randomUUID().toString().substring(0, 8);
        admin = usuario("admin", RoleUsuario.ADMIN, StatusEditor.NENHUM);
    }

    @Test void adminNaoPodeSolicitarEdicao() throws Exception {
        mvc.perform(post("/editores/solicitar").header("Authorization", token(admin)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").isString());

        Usuario salvo = recarregar(admin);
        assertEquals(RoleUsuario.ADMIN, salvo.getRole());
        assertEquals(StatusEditor.NENHUM, salvo.getStatusEditor());
    }

    @Test void aprovarNaoRebaixaAdminComSolicitacaoPendente() throws Exception {
        Usuario outroAdmin = usuario("outro-admin", RoleUsuario.ADMIN, StatusEditor.PENDENTE);

        comoAdmin("/editores/{id}/aprovar", outroAdmin).andExpect(status().isNoContent());

        assertEquals(RoleUsuario.ADMIN, recarregar(outroAdmin).getRole());
    }

    @Test void rejeitarNaoAlteraRoleDeAdmin() throws Exception {
        Usuario outroAdmin = usuario("outro-admin", RoleUsuario.ADMIN, StatusEditor.PENDENTE);

        comoAdmin("/editores/{id}/rejeitar", outroAdmin).andExpect(status().isNoContent());

        assertEquals(RoleUsuario.ADMIN, recarregar(outroAdmin).getRole());
    }

    @Test void revogarNaoRebaixaAdmin() throws Exception {
        Usuario outroAdmin = usuario("outro-admin", RoleUsuario.ADMIN, StatusEditor.APROVADO);

        comoAdmin("/editores/{id}/revogar", outroAdmin).andExpect(status().isNoContent());

        assertEquals(RoleUsuario.ADMIN, recarregar(outroAdmin).getRole());
    }

    @Test void fluxoDeUsuarioComumContinuaPromovendoERevogando() throws Exception {
        Usuario comum = usuario("comum", RoleUsuario.USUARIO, StatusEditor.NENHUM);

        mvc.perform(post("/editores/solicitar").header("Authorization", token(comum)))
                .andExpect(status().isNoContent());
        assertEquals(StatusEditor.PENDENTE, recarregar(comum).getStatusEditor());

        comoAdmin("/editores/{id}/aprovar", comum).andExpect(status().isNoContent());
        Usuario aprovado = recarregar(comum);
        assertEquals(RoleUsuario.EDITOR, aprovado.getRole());
        assertEquals(StatusEditor.APROVADO, aprovado.getStatusEditor());

        comoAdmin("/editores/{id}/revogar", comum).andExpect(status().isNoContent());
        Usuario revogado = recarregar(comum);
        assertEquals(RoleUsuario.USUARIO, revogado.getRole());
        assertEquals(StatusEditor.REVOGADO, revogado.getStatusEditor());
    }

    private ResultActions comoAdmin(String url, Usuario alvo) throws Exception {
        return mvc.perform(patch(url, alvo.getId()).header("Authorization", token(admin)));
    }

    private String token(Usuario usuario) {
        return "Bearer " + jwt.generateAccessToken(usuario);
    }

    private Usuario recarregar(Usuario usuario) {
        entityManager.flush();
        entityManager.clear();
        return usuarios.findById(usuario.getId()).orElseThrow();
    }

    private Usuario usuario(String nome, RoleUsuario role, StatusEditor statusEditor) {
        return usuarios.saveAndFlush(Usuario.builder()
                .nome(nome).email(nome + "-" + sufixo + "@teste.local").password("unused")
                .role(role).statusEditor(statusEditor).build());
    }
}
