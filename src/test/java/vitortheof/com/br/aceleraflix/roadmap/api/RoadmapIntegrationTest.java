package vitortheof.com.br.aceleraflix.roadmap.api;

import com.jayway.jsonpath.JsonPath;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import vitortheof.com.br.aceleraflix.auth.domain.RoleUsuario;
import vitortheof.com.br.aceleraflix.auth.domain.Usuario;
import vitortheof.com.br.aceleraflix.auth.infrastructure.UsuarioRepository;
import vitortheof.com.br.aceleraflix.auth.infrastructure.security.JwtProvider;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Como AvaliacaoVideoIntegrationTest: precisa do Postgres apontado por DB_URL e reverte tudo ao final de cada teste.
@SpringBootTest
@Transactional
class RoadmapIntegrationTest {

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
        admin = usuarios.saveAndFlush(Usuario.builder()
                .nome("admin").email("admin-" + sufixo + "@teste.local").password("unused")
                .role(RoleUsuario.ADMIN).build());
    }

    @Test void roadmapExpoeOrderIndexDeModuloTopicoERecursoNaOrdem() throws Exception {
        String slug = "trilha-" + sufixo;
        String roadmapId = criarRoadmap("Trilha " + sufixo, slug);

        // criados fora de ordem de propósito: a resposta deve vir ordenada por orderIndex
        criar("/roadmaps/" + roadmapId + "/modules", "{\"title\": \"Modulo B\", \"orderIndex\": 2}")
                .andExpect(jsonPath("$.orderIndex").value(2));
        String moduloId = id(criar("/roadmaps/" + roadmapId + "/modules", "{\"title\": \"Modulo A\", \"orderIndex\": 1}")
                .andExpect(jsonPath("$.orderIndex").value(1)));

        criar("/modules/" + moduloId + "/topics", "{\"title\": \"Topico B\", \"orderIndex\": 7}")
                .andExpect(jsonPath("$.orderIndex").value(7));
        String topicoId = id(criar("/modules/" + moduloId + "/topics", "{\"title\": \"Topico A\", \"orderIndex\": 3}")
                .andExpect(jsonPath("$.orderIndex").value(3)));

        criar("/topics/" + topicoId + "/resources",
                "{\"title\": \"Recurso B\", \"type\": \"ARTICLE\", \"externalUrl\": \"https://exemplo.dev/b\", \"orderIndex\": 5}")
                .andExpect(jsonPath("$.orderIndex").value(5));
        criar("/topics/" + topicoId + "/resources",
                "{\"title\": \"Recurso A\", \"type\": \"ARTICLE\", \"externalUrl\": \"https://exemplo.dev/a\", \"orderIndex\": 4}")
                .andExpect(jsonPath("$.orderIndex").value(4));

        limparContexto();

        mvc.perform(get("/roadmap/{slug}", slug))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modules[0].title").value("Modulo A"))
                .andExpect(jsonPath("$.modules[0].orderIndex").value(1))
                .andExpect(jsonPath("$.modules[1].title").value("Modulo B"))
                .andExpect(jsonPath("$.modules[1].orderIndex").value(2))
                .andExpect(jsonPath("$.modules[0].topics[0].title").value("Topico A"))
                .andExpect(jsonPath("$.modules[0].topics[0].orderIndex").value(3))
                .andExpect(jsonPath("$.modules[0].topics[1].orderIndex").value(7))
                .andExpect(jsonPath("$.modules[0].topics[0].resources[0].title").value("Recurso A"))
                .andExpect(jsonPath("$.modules[0].topics[0].resources[0].orderIndex").value(4))
                .andExpect(jsonPath("$.modules[0].topics[0].resources[1].orderIndex").value(5));

        String doRoadmap = "$[?(@.slug=='" + slug + "')]";
        mvc.perform(get("/roadmap"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(doRoadmap + ".modules[0].orderIndex").value(1))
                .andExpect(jsonPath(doRoadmap + ".modules[0].topics[0].orderIndex").value(3))
                .andExpect(jsonPath(doRoadmap + ".modules[0].topics[0].resources[0].orderIndex").value(4));
    }

    @Test void atualizarModuloTopicoERecursoDevolveONovoOrderIndex() throws Exception {
        String roadmapId = criarRoadmap("Trilha " + sufixo, "trilha-" + sufixo);
        String moduloId = id(criar("/roadmaps/" + roadmapId + "/modules", "{\"title\": \"Modulo\", \"orderIndex\": 1}"));
        String topicoId = id(criar("/modules/" + moduloId + "/topics", "{\"title\": \"Topico\", \"orderIndex\": 1}"));
        String recursoId = id(criar("/topics/" + topicoId + "/resources",
                "{\"title\": \"Recurso\", \"type\": \"ARTICLE\", \"orderIndex\": 1}"));

        atualizar("/modules/" + moduloId, "{\"title\": \"Modulo\", \"orderIndex\": 9}")
                .andExpect(status().isOk()).andExpect(jsonPath("$.orderIndex").value(9));
        atualizar("/topics/" + topicoId, "{\"title\": \"Topico\", \"orderIndex\": 8}")
                .andExpect(status().isOk()).andExpect(jsonPath("$.orderIndex").value(8));
        atualizar("/resources/" + recursoId, "{\"title\": \"Recurso\", \"type\": \"ARTICLE\", \"orderIndex\": 6}")
                .andExpect(status().isOk()).andExpect(jsonPath("$.orderIndex").value(6));
    }

    @Test void trocarSlugParaUmJaExistenteRetorna409() throws Exception {
        String existente = "existente-" + sufixo;
        criarRoadmap("Existente", existente);
        String outroId = criarRoadmap("Outro", "outro-" + sufixo);

        atualizar("/roadmap/" + outroId, "{\"title\": \"Outro\", \"slug\": \"" + existente + "\"}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.mensagem").isString());

        // manter o próprio slug continua permitido
        atualizar("/roadmap/" + outroId, "{\"title\": \"Outro editado\", \"slug\": \"outro-" + sufixo + "\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Outro editado"));
    }

    @Test void roadmapComTituloOuSlugInvalidoRetorna400() throws Exception {
        String roadmapId = criarRoadmap("Trilha " + sufixo, "trilha-" + sufixo);
        String longo = "a".repeat(256);
        String[][] corposECampos = {
                { "{\"slug\": \"ok-" + sufixo + "\"}", "title" },
                { "{\"title\": \"  \", \"slug\": \"ok-" + sufixo + "\"}", "title" },
                { "{\"title\": \"" + longo + "\", \"slug\": \"ok-" + sufixo + "\"}", "title" },
                { "{\"title\": \"Ok\"}", "slug" },
                { "{\"title\": \"Ok\", \"slug\": \"\"}", "slug" },
                { "{\"title\": \"Ok\", \"slug\": \"Com Espaco\"}", "slug" },
                { "{\"title\": \"Ok\", \"slug\": \"com_underline\"}", "slug" },
                { "{\"title\": \"Ok\", \"slug\": \"" + longo + "\"}", "slug" },
        };
        for (String[] caso : corposECampos) {
            mvc.perform(comoAdmin(post("/roadmap")).contentType(MediaType.APPLICATION_JSON).content(caso[0]))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$." + caso[1]).isString());
            atualizar("/roadmap/" + roadmapId, caso[0])
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$." + caso[1]).isString());
        }
    }

    @Test void moduloTopicoERecursoInvalidosRetornam400() throws Exception {
        String roadmapId = criarRoadmap("Trilha " + sufixo, "trilha-" + sufixo);
        String moduloId = id(criar("/roadmaps/" + roadmapId + "/modules", "{\"title\": \"Modulo\", \"orderIndex\": 1}"));
        String topicoId = id(criar("/modules/" + moduloId + "/topics", "{\"title\": \"Topico\", \"orderIndex\": 1}"));
        String recursoId = id(criar("/topics/" + topicoId + "/resources",
                "{\"title\": \"Recurso\", \"type\": \"ARTICLE\", \"orderIndex\": 1}"));

        String semTitulo = "{\"orderIndex\": 1}";
        String semOrdem = "{\"title\": \"Titulo\"}";
        String recursoSemTipo = "{\"title\": \"Recurso\", \"orderIndex\": 1}";

        String[] criacoes = {
                "/roadmaps/" + roadmapId + "/modules", "/modules/" + moduloId + "/topics", "/topics/" + topicoId + "/resources" };
        for (String url : criacoes) {
            mvc.perform(comoAdmin(post(url)).contentType(MediaType.APPLICATION_JSON).content(semTitulo))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.title").isString());
            mvc.perform(comoAdmin(post(url)).contentType(MediaType.APPLICATION_JSON).content(semOrdem))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.orderIndex").isString());
        }
        String[] atualizacoes = { "/modules/" + moduloId, "/topics/" + topicoId, "/resources/" + recursoId };
        for (String url : atualizacoes) {
            atualizar(url, semTitulo).andExpect(status().isBadRequest()).andExpect(jsonPath("$.title").isString());
            atualizar(url, semOrdem).andExpect(status().isBadRequest()).andExpect(jsonPath("$.orderIndex").isString());
        }
        mvc.perform(comoAdmin(post("/topics/" + topicoId + "/resources"))
                        .contentType(MediaType.APPLICATION_JSON).content(recursoSemTipo))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.type").isString());
        atualizar("/resources/" + recursoId, recursoSemTipo)
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.type").isString());
    }

    @Test void criarVideoSemCategoriaRetorna400() throws Exception {
        for (String corpo : new String[] {
                "{\"titulo\": \"Video\", \"urlYoutube\": \"https://www.youtube.com/watch?v=dQw4w9WgXcQ\"}",
                "{\"titulo\": \"Video\", \"urlYoutube\": \"https://www.youtube.com/watch?v=dQw4w9WgXcQ\", \"categoriaId\": null}" }) {
            mvc.perform(comoAdmin(post("/videos")).contentType(MediaType.APPLICATION_JSON).content(corpo))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.categoriaId").isString());
        }
    }

    private String criarRoadmap(String titulo, String slug) throws Exception {
        return id(criar("/roadmap", "{\"title\": \"" + titulo + "\", \"slug\": \"" + slug + "\", \"description\": \"desc\"}"));
    }

    private ResultActions criar(String url, String corpo) throws Exception {
        return mvc.perform(comoAdmin(post(url)).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated());
    }

    private ResultActions atualizar(String url, String corpo) throws Exception {
        return mvc.perform(comoAdmin(put(url)).contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private String id(ResultActions resultado) throws Exception {
        return JsonPath.read(resultado.andReturn().getResponse().getContentAsString(), "$.id");
    }

    // As coleções do roadmap só são recarregadas do banco (com @OrderBy) fora do contexto de persistência do teste.
    private void limparContexto() {
        entityManager.flush();
        entityManager.clear();
    }

    private MockHttpServletRequestBuilder comoAdmin(MockHttpServletRequestBuilder request) {
        return request.header("Authorization", "Bearer " + jwt.generateAccessToken(admin));
    }
}
