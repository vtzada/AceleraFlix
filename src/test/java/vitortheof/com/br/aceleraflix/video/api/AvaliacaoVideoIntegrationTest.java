package vitortheof.com.br.aceleraflix.video.api;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
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
import vitortheof.com.br.aceleraflix.auth.domain.Usuario;
import vitortheof.com.br.aceleraflix.auth.infrastructure.UsuarioRepository;
import vitortheof.com.br.aceleraflix.auth.infrastructure.security.JwtProvider;
import vitortheof.com.br.aceleraflix.category.domain.Categoria;
import vitortheof.com.br.aceleraflix.category.infrastructure.CategoriaRepository;
import vitortheof.com.br.aceleraflix.video.domain.Video;
import vitortheof.com.br.aceleraflix.video.infrastructure.AvaliacaoVideoRepository;
import vitortheof.com.br.aceleraflix.video.infrastructure.VideoRepository;

import java.util.UUID;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Como AceleraflixApplicationTests, precisa do Postgres apontado por DB_URL (Flyway aplica as migrações).
// Cada teste roda numa transação revertida ao final, então não deixa dados no banco.
@SpringBootTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@Transactional
class AvaliacaoVideoIntegrationTest {
    @Autowired WebApplicationContext context;
    @Autowired JwtProvider jwt;
    @Autowired UsuarioRepository usuarios;
    @Autowired CategoriaRepository categorias;
    @Autowired VideoRepository videos;
    @Autowired AvaliacaoVideoRepository avaliacoes;
    @Autowired EntityManagerFactory entityManagerFactory;

    MockMvc mvc;
    String sufixo;
    Usuario ana, bruno, carla;
    Categoria categoria;
    Video video;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        sufixo = UUID.randomUUID().toString().substring(0, 8);
        ana = usuario("ana");
        bruno = usuario("bruno");
        carla = usuario("carla");
        categoria = categorias.saveAndFlush(Categoria.builder()
                .nome("Categoria " + sufixo).slug("categoria-" + sufixo).criadoPor(ana.getId()).build());
        video = video("Video " + sufixo);
    }

    @Test void criarAvaliacaoDevolveResumoAtualizado() throws Exception {
        avaliar(ana, video.getId(), "{\"nota\": 4}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mediaAvaliacoes").value(4.0))
                .andExpect(jsonPath("$.totalAvaliacoes").value(1))
                .andExpect(jsonPath("$.notaUsuario").value(4));
        assertEquals(1, avaliacoes.count());
    }

    @Test void avaliarDeNovoAlteraANotaSemDuplicar() throws Exception {
        avaliar(ana, video.getId(), "{\"nota\": 4}").andExpect(status().isOk());
        avaliar(ana, video.getId(), "{\"nota\": 2}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mediaAvaliacoes").value(2.0))
                .andExpect(jsonPath("$.totalAvaliacoes").value(1))
                .andExpect(jsonPath("$.notaUsuario").value(2));
        assertEquals(1, avaliacoes.count());
    }

    @Test void removerApagaSoAPropriaAvaliacaoEEhIdempotente() throws Exception {
        avaliar(ana, video.getId(), "{\"nota\": 5}").andExpect(status().isOk());
        avaliar(bruno, video.getId(), "{\"nota\": 3}").andExpect(status().isOk());

        remover(ana, video.getId())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mediaAvaliacoes").value(3.0))
                .andExpect(jsonPath("$.totalAvaliacoes").value(1))
                .andExpect(jsonPath("$.notaUsuario").value(nullValue()));
        remover(bruno, video.getId())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"mediaAvaliacoes\":null")))
                .andExpect(jsonPath("$.totalAvaliacoes").value(0))
                .andExpect(content().string(containsString("\"notaUsuario\":null")));
        remover(bruno, video.getId())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAvaliacoes").value(0));
        assertEquals(0, avaliacoes.count());
    }

    @Test void notaForaDeUmACincoRetorna400() throws Exception {
        for (String corpo : new String[] { "{\"nota\": 0}", "{\"nota\": 6}", "{\"nota\": -1}", "{\"nota\": null}", "{}",
                "{\"nota\": 4.5}", "{\"nota\": 10}" }) {
            avaliar(ana, video.getId(), corpo)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.nota").isString());
        }
        for (String corpo : new String[] { "{\"nota\": \"abc\"}", "", "nao-e-json" }) {
            avaliar(ana, video.getId(), corpo)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.mensagem").isString());
        }
        assertEquals(0, avaliacoes.count());
    }

    @Test void anonimoRecebe401AoAvaliarERemover() throws Exception {
        mvc.perform(put("/videos/{id}/avaliacao", video.getId())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nota\": 4}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(delete("/videos/{id}/avaliacao", video.getId()))
                .andExpect(status().isUnauthorized());
        mvc.perform(put("/videos/{id}/avaliacao", video.getId())
                        .header("Authorization", "Bearer token-invalido")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nota\": 4}"))
                .andExpect(status().isUnauthorized());
        assertEquals(0, avaliacoes.count());
    }

    @Test void videoInexistenteRetorna404() throws Exception {
        UUID inexistente = UUID.randomUUID();
        avaliar(ana, inexistente, "{\"nota\": 4}")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensagem").isString());
        remover(ana, inexistente)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test void detalheTrazMediaTotalENotaDoUsuarioLogado() throws Exception {
        avaliar(ana, video.getId(), "{\"nota\": 5}").andExpect(status().isOk());
        avaliar(bruno, video.getId(), "{\"nota\": 4}").andExpect(status().isOk());

        // logado que avaliou
        mvc.perform(autenticado(get("/videos/{id}", video.getId()), ana))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value(video.getTitulo()))
                .andExpect(jsonPath("$.mediaAvaliacoes").value(4.5))
                .andExpect(jsonPath("$.totalAvaliacoes").value(2))
                .andExpect(jsonPath("$.notaUsuario").value(5));
        // logado que não avaliou
        mvc.perform(autenticado(get("/videos/{id}", video.getId()), carla))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mediaAvaliacoes").value(4.5))
                .andExpect(jsonPath("$.totalAvaliacoes").value(2))
                .andExpect(content().string(containsString("\"notaUsuario\":null")));
        // anônimo continua lendo
        mvc.perform(get("/videos/{id}", video.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mediaAvaliacoes").value(4.5))
                .andExpect(jsonPath("$.totalAvaliacoes").value(2))
                .andExpect(content().string(containsString("\"notaUsuario\":null")));
    }

    @Test void mediaEhArredondadaAUmaCasaDecimal() throws Exception {
        avaliar(ana, video.getId(), "{\"nota\": 5}").andExpect(status().isOk());
        avaliar(bruno, video.getId(), "{\"nota\": 4}").andExpect(status().isOk());
        avaliar(carla, video.getId(), "{\"nota\": 4}")
                .andExpect(jsonPath("$.mediaAvaliacoes").value(4.3))
                .andExpect(jsonPath("$.totalAvaliacoes").value(3));
        avaliar(carla, video.getId(), "{\"nota\": 5}")
                .andExpect(jsonPath("$.mediaAvaliacoes").value(4.7))
                .andExpect(jsonPath("$.totalAvaliacoes").value(3));
    }

    @Test void videoSemAvaliacoesTemTotalZeroEMediaNull() throws Exception {
        mvc.perform(autenticado(get("/videos/{id}", video.getId()), ana))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"mediaAvaliacoes\":null")))
                .andExpect(jsonPath("$.totalAvaliacoes").value(0))
                .andExpect(content().string(containsString("\"notaUsuario\":null")));
    }

    @Test void listagensTrazemAvaliacoesPorVideo() throws Exception {
        Video semAvaliacao = video("Outro " + sufixo);
        avaliar(ana, video.getId(), "{\"nota\": 5}").andExpect(status().isOk());
        avaliar(bruno, video.getId(), "{\"nota\": 2}").andExpect(status().isOk());

        String avaliado = "$.content[?(@.id=='" + video.getId() + "')]";
        String naoAvaliado = "$.content[?(@.id=='" + semAvaliacao.getId() + "')]";
        String[] listagens = {
                "/videos",
                "/videos?q=" + sufixo,
                "/videos?criadoPor=" + ana.getId(),
                "/videos/categoria/" + categoria.getId()
        };
        for (String url : listagens) {
            mvc.perform(autenticado(get(url), bruno))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(avaliado + ".mediaAvaliacoes").value(contains(3.5)))
                    .andExpect(jsonPath(avaliado + ".totalAvaliacoes").value(contains(2)))
                    .andExpect(jsonPath(avaliado + ".notaUsuario").value(contains(2)))
                    .andExpect(jsonPath(naoAvaliado + ".mediaAvaliacoes").value(contains(nullValue())))
                    .andExpect(jsonPath(naoAvaliado + ".totalAvaliacoes").value(contains(0)))
                    .andExpect(jsonPath(naoAvaliado + ".notaUsuario").value(contains(nullValue())));
            mvc.perform(get(url))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(avaliado + ".mediaAvaliacoes").value(contains(3.5)))
                    .andExpect(jsonPath(avaliado + ".totalAvaliacoes").value(contains(2)))
                    .andExpect(jsonPath(avaliado + ".notaUsuario").value(contains(nullValue())));
        }
    }

    @Test void listagemAgregaAvaliacoesEmLoteSemConsultaPorVideo() throws Exception {
        String url = "/videos/categoria/" + categoria.getId();
        avaliar(ana, video.getId(), "{\"nota\": 5}").andExpect(status().isOk());
        long consultasComUmVideo = consultasAoListar(url);

        for (int i = 0; i < 4; i++) {
            Video outro = video("Lote " + i + " " + sufixo);
            avaliar(ana, outro.getId(), "{\"nota\": 3}").andExpect(status().isOk());
            avaliar(bruno, outro.getId(), "{\"nota\": 4}").andExpect(status().isOk());
        }
        assertEquals(consultasComUmVideo, consultasAoListar(url));
    }

    private long consultasAoListar(String url) throws Exception {
        Statistics estatisticas = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        estatisticas.clear();
        mvc.perform(autenticado(get(url), ana)).andExpect(status().isOk());
        return estatisticas.getQueryExecutionCount();
    }

    private ResultActions avaliar(Usuario usuario, UUID videoId, String corpo) throws Exception {
        return mvc.perform(autenticado(put("/videos/{id}/avaliacao", videoId), usuario)
                .contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private ResultActions remover(Usuario usuario, UUID videoId) throws Exception {
        return mvc.perform(autenticado(delete("/videos/{id}/avaliacao", videoId), usuario));
    }

    private MockHttpServletRequestBuilder autenticado(MockHttpServletRequestBuilder request, Usuario usuario) {
        return request.header("Authorization", "Bearer " + jwt.generateAccessToken(usuario));
    }

    private Usuario usuario(String nome) {
        return usuarios.saveAndFlush(Usuario.builder()
                .nome(nome).email(nome + "-" + sufixo + "@teste.local").password("unused").build());
    }

    private Video video(String titulo) {
        return videos.saveAndFlush(Video.builder()
                .titulo(titulo)
                .urlEmbed("https://www.youtube.com/watch?v=dQw4w9WgXcQ")
                .videoExternoId("dQw4w9WgXcQ")
                .categoriaId(categoria.getId())
                .criadoPor(ana.getId())
                .build());
    }
}
