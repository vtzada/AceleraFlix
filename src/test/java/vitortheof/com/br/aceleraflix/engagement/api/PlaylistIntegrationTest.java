package vitortheof.com.br.aceleraflix.engagement.api;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import vitortheof.com.br.aceleraflix.auth.domain.Usuario;
import vitortheof.com.br.aceleraflix.auth.infrastructure.UsuarioRepository;
import vitortheof.com.br.aceleraflix.auth.infrastructure.security.JwtProvider;
import vitortheof.com.br.aceleraflix.category.domain.Categoria;
import vitortheof.com.br.aceleraflix.category.infrastructure.CategoriaRepository;
import vitortheof.com.br.aceleraflix.engagement.domain.Playlist;
import vitortheof.com.br.aceleraflix.engagement.domain.PlaylistType;
import vitortheof.com.br.aceleraflix.engagement.infrastructure.PlaylistRepository;
import vitortheof.com.br.aceleraflix.video.domain.Video;
import vitortheof.com.br.aceleraflix.video.infrastructure.VideoRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Como AvaliacaoVideoIntegrationTest: precisa do Postgres apontado por DB_URL e reverte tudo ao final de cada teste.
// A exceção é o teste de concorrência, que precisa de dados commitados e limpa o que criou.
@SpringBootTest
@Transactional
class PlaylistIntegrationTest {

    static final String[] ENDPOINTS = { "/playlists/watch-later/{videoId}", "/playlists/liked/{videoId}" };

    @Autowired WebApplicationContext context;
    @Autowired JwtProvider jwt;
    @Autowired UsuarioRepository usuarios;
    @Autowired CategoriaRepository categorias;
    @Autowired VideoRepository videos;
    @Autowired PlaylistRepository playlists;
    @Autowired EntityManager entityManager;

    MockMvc mvc;
    String sufixo;
    Usuario ana;
    Categoria categoria;
    Video video;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        sufixo = UUID.randomUUID().toString().substring(0, 8);
        ana = usuarios.saveAndFlush(Usuario.builder()
                .nome("ana").email("ana-" + sufixo + "@teste.local").password("unused").build());
        categoria = categorias.saveAndFlush(Categoria.builder()
                .nome("Categoria " + sufixo).slug("categoria-" + sufixo).criadoPor(ana.getId()).build());
        video = video("Video " + sufixo);
    }

    @Test void toggleAdicionaERemoveSemDuplicarAPlaylistDeSistema() throws Exception {
        Video outro = video("Outro " + sufixo);
        for (String endpoint : ENDPOINTS) {
            alternar(endpoint, video.getId()).andExpect(status().isOk());
            alternar(endpoint, outro.getId()).andExpect(status().isOk());
        }
        limparContexto();

        String curtidos = "$[?(@.type=='LIKED')]";
        String maisTarde = "$[?(@.type=='WATCH_LATER')]";
        mvc.perform(get("/playlists").header("Authorization", token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath(curtidos, hasSize(1)))
                .andExpect(jsonPath(maisTarde, hasSize(1)))
                .andExpect(jsonPath(curtidos + ".videos[*].videoId")
                        .value(contains(video.getId().toString(), outro.getId().toString())))
                .andExpect(jsonPath(maisTarde + ".videos[*].videoId")
                        .value(contains(video.getId().toString(), outro.getId().toString())));

        for (String endpoint : ENDPOINTS) {
            alternar(endpoint, video.getId()).andExpect(status().isOk());
        }
        limparContexto();

        mvc.perform(get("/playlists").header("Authorization", token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath(curtidos + ".videos[*].videoId").value(contains(outro.getId().toString())))
                .andExpect(jsonPath(maisTarde + ".videos[*].videoId").value(contains(outro.getId().toString())));
    }

    @Test void videoInexistenteRetorna404ENaoEntraNaPlaylist() throws Exception {
        UUID inexistente = UUID.randomUUID();
        for (String endpoint : ENDPOINTS) {
            alternar(endpoint, inexistente)
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.mensagem").isString());
        }
        limparContexto();

        for (Playlist playlist : playlists.findAllByUserId(ana.getId())) {
            assertEquals(0, playlist.getVideos().size());
        }
    }

    @Test void videoApagadoDepoisDeAdicionadoAindaPodeSerRemovidoDaPlaylist() throws Exception {
        alternar(ENDPOINTS[1], video.getId()).andExpect(status().isOk());
        limparContexto();
        videos.deleteById(video.getId());
        videos.flush();

        alternar(ENDPOINTS[1], video.getId()).andExpect(status().isOk());
        limparContexto();

        assertEquals(0, playlists.findByUserIdAndType(ana.getId(), PlaylistType.LIKED).orElseThrow().getVideos().size());
    }

    // Sem transação de teste: as requisições simultâneas precisam enxergar dados commitados.
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void requisicoesSimultaneasCriamUmaUnicaPlaylistDeSistemaSemErro() throws Exception {
        int simultaneas = 8;
        List<Video> criados = new ArrayList<>(List.of(video));
        for (int i = 1; i < simultaneas; i++) {
            criados.add(video("Simultaneo " + i + " " + sufixo));
        }
        try {
            for (String endpoint : ENDPOINTS) {
                // primeira requisição do usuário para o tipo: todas disputam a criação da playlist
                List<Callable<Integer>> adicoes = new ArrayList<>();
                for (Video v : criados) {
                    adicoes.add(() -> statusDe(endpoint, v.getId()));
                }
                assertEquals(List.of(200, 200, 200, 200, 200, 200, 200, 200), emParalelo(adicoes));
            }
            for (PlaylistType tipo : new PlaylistType[] { PlaylistType.WATCH_LATER, PlaylistType.LIKED }) {
                List<Playlist> doTipo = playlists.findAllByUserId(ana.getId()).stream()
                        .filter(p -> p.getType() == tipo).toList();
                assertEquals(1, doTipo.size());
                assertEquals(simultaneas, doTipo.get(0).getVideos().size());
            }

            // toggles simultâneos do MESMO vídeo (já curtido): nenhum 500 e, sendo um número par de toggles
            // serializados, o vídeo termina como começou, dentro da playlist e sem duplicar
            List<Callable<Integer>> mesmoVideo = new ArrayList<>();
            for (int i = 0; i < simultaneas; i++) {
                mesmoVideo.add(() -> statusDe(ENDPOINTS[1], video.getId()));
            }
            assertEquals(List.of(200, 200, 200, 200, 200, 200, 200, 200), emParalelo(mesmoVideo));
            Playlist curtidos = playlists.findByUserIdAndType(ana.getId(), PlaylistType.LIKED).orElseThrow();
            assertEquals(simultaneas, curtidos.getVideos().size());
            assertEquals(1, curtidos.getVideos().stream().filter(pv -> pv.getVideoId().equals(video.getId())).count());
        } finally {
            playlists.deleteAll(playlists.findAllByUserId(ana.getId()));
            videos.deleteAll(criados);
            categorias.delete(categoria);
            usuarios.delete(ana);
        }
    }

    private List<Integer> emParalelo(List<Callable<Integer>> tarefas) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(tarefas.size());
        try {
            CyclicBarrier largada = new CyclicBarrier(tarefas.size());
            List<Future<Integer>> futuros = new ArrayList<>();
            for (Callable<Integer> tarefa : tarefas) {
                futuros.add(executor.submit(() -> {
                    largada.await(10, TimeUnit.SECONDS);
                    return tarefa.call();
                }));
            }
            List<Integer> resultados = new ArrayList<>();
            for (Future<Integer> futuro : futuros) {
                resultados.add(futuro.get(30, TimeUnit.SECONDS));
            }
            return resultados;
        } finally {
            executor.shutdownNow();
        }
    }

    private int statusDe(String endpoint, UUID videoId) throws Exception {
        return alternar(endpoint, videoId).andReturn().getResponse().getStatus();
    }

    private ResultActions alternar(String endpoint, UUID videoId) throws Exception {
        return mvc.perform(post(endpoint, videoId).header("Authorization", token()));
    }

    private String token() {
        return "Bearer " + jwt.generateAccessToken(ana);
    }

    private void limparContexto() {
        entityManager.flush();
        entityManager.clear();
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
