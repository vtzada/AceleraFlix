package vitortheof.com.br.aceleraflix.video.api;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import vitortheof.com.br.aceleraflix.auth.domain.Usuario;
import vitortheof.com.br.aceleraflix.auth.infrastructure.UsuarioRepository;
import vitortheof.com.br.aceleraflix.category.domain.Categoria;
import vitortheof.com.br.aceleraflix.category.infrastructure.CategoriaRepository;
import vitortheof.com.br.aceleraflix.video.domain.Tag;
import vitortheof.com.br.aceleraflix.video.domain.Video;
import vitortheof.com.br.aceleraflix.video.infrastructure.TagRepository;
import vitortheof.com.br.aceleraflix.video.infrastructure.VideoRepository;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Como AvaliacaoVideoIntegrationTest: precisa do Postgres apontado por DB_URL e reverte tudo ao final de cada teste.
@SpringBootTest
@Transactional
class VideoSearchIntegrationTest {

    @Autowired WebApplicationContext context;
    @Autowired UsuarioRepository usuarios;
    @Autowired CategoriaRepository categorias;
    @Autowired VideoRepository videos;
    @Autowired TagRepository tags;

    MockMvc mvc;
    String sufixo;
    Usuario ana;
    Categoria categoria;
    Tag tagA, tagB;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        sufixo = UUID.randomUUID().toString().substring(0, 8);
        ana = usuarios.saveAndFlush(Usuario.builder()
                .nome("ana").email("ana-" + sufixo + "@teste.local").password("unused").build());
        categoria = categorias.saveAndFlush(Categoria.builder()
                .nome("Categoria " + sufixo).slug("categoria-" + sufixo).criadoPor(ana.getId()).build());
        // duas tags que casam com o mesmo termo de busca
        tagA = tags.saveAndFlush(Tag.builder().nome(sufixo + "-a").build());
        tagB = tags.saveAndFlush(Tag.builder().nome(sufixo + "-b").build());
    }

    @Test void videoComDuasTagsQueCasamApareceUmaVezNaBusca() throws Exception {
        Video video = video("Sem o termo no titulo", Set.of(tagA, tagB));

        mvc.perform(get("/videos").param("q", sufixo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(video.getId().toString()))
                .andExpect(jsonPath("$.page.totalElements").value(1))
                .andExpect(jsonPath("$.page.totalPages").value(1));
    }

    @Test void videoQueCasaPorTituloDescricaoETagsApareceUmaVez() throws Exception {
        Video video = videos.saveAndFlush(base("Titulo " + sufixo)
                .descricao("Descricao " + sufixo).tags(new HashSet<>(Set.of(tagA, tagB))).build());

        mvc.perform(get("/videos").param("q", sufixo.toUpperCase()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(video.getId().toString()))
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }

    @Test void paginacaoDaBuscaContaCadaVideoUmaVez() throws Exception {
        video("Primeiro", Set.of(tagA, tagB));
        video("Segundo", Set.of(tagA, tagB));
        video("Sem tags " + sufixo, Set.of());

        String pagina0 = mvc.perform(get("/videos").param("q", sufixo).param("size", "1").param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.page.totalElements").value(3))
                .andExpect(jsonPath("$.page.totalPages").value(3))
                .andReturn().getResponse().getContentAsString();
        String pagina1 = mvc.perform(get("/videos").param("q", sufixo).param("size", "1").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.page.totalElements").value(3))
                .andReturn().getResponse().getContentAsString();

        assertNotEquals((String) JsonPath.read(pagina0, "$.content[0].id"), JsonPath.read(pagina1, "$.content[0].id"));

        mvc.perform(get("/videos").param("q", sufixo).param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.page.totalElements").value(3))
                .andExpect(jsonPath("$.page.totalPages").value(1));
    }

    private Video video(String titulo, Set<Tag> tagsDoVideo) {
        return videos.saveAndFlush(base(titulo).tags(new HashSet<>(tagsDoVideo)).build());
    }

    private Video.VideoBuilder base(String titulo) {
        return Video.builder()
                .titulo(titulo)
                .urlEmbed("https://www.youtube.com/watch?v=dQw4w9WgXcQ")
                .videoExternoId("dQw4w9WgXcQ")
                .categoriaId(categoria.getId())
                .criadoPor(ana.getId());
    }
}
