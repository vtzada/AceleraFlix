package vitortheof.com.br.aceleraflix.engagement.infrastructure;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

// Roda as migrações num schema descartável do Postgres apontado por DB_URL: aplica até a V14,
// insere playlists de sistema duplicadas (o estado que o bug produzia) e só então aplica a V15.
@SpringBootTest
class PlaylistMigrationV15Test {

    @Autowired DataSource dataSource;

    JdbcTemplate jdbc;
    String schema;

    @BeforeEach
    void setup() {
        jdbc = new JdbcTemplate(dataSource);
        schema = "v15_teste_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    @AfterEach
    void cleanup() {
        jdbc.execute("DROP SCHEMA IF EXISTS " + schema + " CASCADE");
    }

    @Test void v15FundeDuplicadasPreservandoOsVideosECriaOIndiceUnico() {
        migrarAte("14");

        UUID ana = UUID.randomUUID(), bruno = UUID.randomUUID();
        UUID a = UUID.randomUUID(), b = UUID.randomUUID(), c = UUID.randomUUID(), d = UUID.randomUUID(), e = UUID.randomUUID();
        LocalDateTime antes = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime depois = LocalDateTime.of(2026, 2, 1, 10, 0);

        // ana: três LIKED, uma WATCH_LATER e duas CUSTOM. O vídeo b está na que fica e numa descartada;
        // o vídeo e só existe nas duas descartadas, com datas diferentes
        UUID curtidos1 = playlist("00000000-0000-0000-0000-000000000001", ana, "LIKED");
        UUID curtidos2 = playlist("00000000-0000-0000-0000-000000000002", ana, "LIKED");
        UUID curtidos3 = playlist("00000000-0000-0000-0000-000000000003", ana, "LIKED");
        UUID maisTardeAna = playlist("00000000-0000-0000-0000-000000000004", ana, "WATCH_LATER");
        UUID custom1 = playlist("00000000-0000-0000-0000-000000000005", ana, "CUSTOM");
        UUID custom2 = playlist("00000000-0000-0000-0000-000000000006", ana, "CUSTOM");
        video(curtidos1, a, antes);
        video(curtidos1, b, depois);
        video(curtidos2, b, antes);
        video(curtidos2, c, depois);
        video(curtidos2, e, depois);
        video(curtidos3, e, antes);
        video(maisTardeAna, a, antes);
        video(custom1, a, antes);
        video(custom2, a, antes);
        // bruno: duas WATCH_LATER com o mesmo vídeo; a de maior id foi criada primeiro
        UUID maisTarde1 = playlist("00000000-0000-0000-0000-000000000007", bruno, "WATCH_LATER");
        UUID maisTarde2 = playlist("00000000-0000-0000-0000-000000000008", bruno, "WATCH_LATER");
        video(maisTarde1, d, depois);
        video(maisTarde2, d, antes);

        migrarAte("15");

        assertEquals(List.of(curtidos1), playlists(ana, "LIKED"));
        assertEquals(List.of(a, b, c, e).stream().sorted().toList(), videos(curtidos1));
        assertEquals(List.of(maisTardeAna), playlists(ana, "WATCH_LATER"));
        assertEquals(List.of(a), videos(maisTardeAna));
        assertEquals(List.of(custom1, custom2), playlists(ana, "CUSTOM"));
        assertEquals(List.of(a), videos(custom1));
        assertEquals(List.of(a), videos(custom2));
        assertEquals(List.of(maisTarde1), playlists(bruno, "WATCH_LATER"));
        assertEquals(List.of(d), videos(maisTarde1));
        // o vídeo que já estava na playlist mantida não é sobrescrito
        assertEquals(Timestamp.valueOf(depois), jdbc.queryForObject(
                "SELECT added_at FROM " + schema + ".playlist_videos WHERE playlist_id = ? AND video_id = ?",
                Timestamp.class, curtidos1, b));
        // o vídeo que só existia nas descartadas entra uma única vez, com a data mais antiga entre elas
        assertEquals(Timestamp.valueOf(antes), jdbc.queryForObject(
                "SELECT added_at FROM " + schema + ".playlist_videos WHERE playlist_id = ? AND video_id = ?",
                Timestamp.class, curtidos1, e));
        assertEquals(5, jdbc.queryForObject("SELECT count(*) FROM " + schema + ".playlist", Integer.class));
        assertEquals(8, jdbc.queryForObject("SELECT count(*) FROM " + schema + ".playlist_videos", Integer.class));

        // reiniciar a aplicação roda o Flyway de novo: nada a aplicar e os dados consolidados não mudam
        List<String> playlistsAntes = linhas("SELECT id || ' ' || user_id || ' ' || type FROM " + schema + ".playlist ORDER BY id");
        List<String> videosAntes = linhas("SELECT id || ' ' || playlist_id || ' ' || video_id || ' ' || added_at FROM "
                + schema + ".playlist_videos ORDER BY id");
        assertEquals(0, migrarAte("15").migrationsExecuted);
        assertEquals(playlistsAntes, linhas("SELECT id || ' ' || user_id || ' ' || type FROM " + schema + ".playlist ORDER BY id"));
        assertEquals(videosAntes, linhas("SELECT id || ' ' || playlist_id || ' ' || video_id || ' ' || added_at FROM "
                + schema + ".playlist_videos ORDER BY id"));
        assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM " + schema
                + ".flyway_schema_history WHERE version = '15' AND success", Integer.class));

        // índice único parcial: nova duplicada de sistema é rejeitada, CUSTOM continua livre
        assertThrows(DuplicateKeyException.class, () -> playlist(UUID.randomUUID().toString(), ana, "LIKED"));
        assertThrows(DuplicateKeyException.class, () -> playlist(UUID.randomUUID().toString(), bruno, "WATCH_LATER"));
        playlist(UUID.randomUUID().toString(), ana, "CUSTOM");
        playlist(UUID.randomUUID().toString(), bruno, "LIKED");
    }

    @Test void v15AplicaSemDuplicadas() {
        migrarAte("14");
        UUID ana = UUID.randomUUID();
        UUID curtidos = playlist(UUID.randomUUID().toString(), ana, "LIKED");
        video(curtidos, UUID.randomUUID(), LocalDateTime.now());

        migrarAte("15");

        assertEquals(List.of(curtidos), playlists(ana, "LIKED"));
        assertEquals(1, videos(curtidos).size());
    }

    // Simula uma instância antiga ainda gravando durante a implantação: uma transação aberta adiciona um vídeo
    // à playlist duplicada (que será apagada) e só confirma depois que a V15 já começou. A migração tem de
    // esperar essa escrita antes de levantar as duplicadas, senão o vídeo confirmado some no DELETE em cascata.
    @Test void v15EsperaEscritaConcorrenteENaoPerdeOVideoConfirmadoDuranteAFusao() throws Exception {
        migrarAte("14");
        UUID ana = UUID.randomUUID();
        UUID mantida = playlist("00000000-0000-0000-0000-000000000001", ana, "LIKED");
        UUID duplicada = playlist("00000000-0000-0000-0000-000000000002", ana, "LIKED");
        UUID antigo = UUID.randomUUID(), adicionadoDuranteAMigracao = UUID.randomUUID();
        video(duplicada, antigo, LocalDateTime.of(2026, 1, 1, 10, 0));

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try (Connection escritor = dataSource.getConnection()) {
            escritor.setAutoCommit(false);
            try (PreparedStatement insert = escritor.prepareStatement(
                    "INSERT INTO " + schema + ".playlist_videos (id, playlist_id, video_id) VALUES (?, ?, ?)")) {
                insert.setObject(1, UUID.randomUUID());
                insert.setObject(2, duplicada);
                insert.setObject(3, adicionadoDuranteAMigracao);
                insert.executeUpdate();
            }
            Future<MigrateResult> migracao = executor.submit(() -> migrarAte("15"));
            aguardarMigracaoBloqueada();
            escritor.commit();
            assertEquals(1, migracao.get(30, TimeUnit.SECONDS).migrationsExecuted);
        } finally {
            executor.shutdownNow();
        }

        assertEquals(List.of(mantida), playlists(ana, "LIKED"));
        assertEquals(List.of(antigo, adicionadoDuranteAMigracao).stream().sorted().toList(), videos(mantida));
    }

    private void aguardarMigracaoBloqueada() throws InterruptedException {
        for (int tentativa = 0; tentativa < 200; tentativa++) {
            if (jdbc.queryForObject("SELECT count(*) FROM pg_locks WHERE NOT granted", Integer.class) > 0) {
                return;
            }
            Thread.sleep(50);
        }
        fail("A V15 não chegou a esperar pela escrita concorrente");
    }

    private List<String> linhas(String sql) {
        return jdbc.queryForList(sql, String.class);
    }

    private MigrateResult migrarAte(String versao) {
        return Flyway.configure()
                .dataSource(dataSource)
                .schemas(schema)
                .defaultSchema(schema)
                .locations("classpath:db/migration")
                .target(versao)
                .load()
                .migrate();
    }

    private UUID playlist(String id, UUID userId, String type) {
        UUID playlistId = UUID.fromString(id);
        jdbc.update("INSERT INTO " + schema + ".playlist (id, user_id, title, type) VALUES (?, ?, ?, ?)",
                playlistId, userId, "Playlist", type);
        return playlistId;
    }

    private void video(UUID playlistId, UUID videoId, LocalDateTime addedAt) {
        jdbc.update("INSERT INTO " + schema + ".playlist_videos (id, playlist_id, video_id, added_at) VALUES (?, ?, ?, ?)",
                UUID.randomUUID(), playlistId, videoId, Timestamp.valueOf(addedAt));
    }

    private List<UUID> playlists(UUID userId, String type) {
        return jdbc.queryForList("SELECT id FROM " + schema + ".playlist WHERE user_id = ? AND type = ? ORDER BY id",
                UUID.class, userId, type);
    }

    private List<UUID> videos(UUID playlistId) {
        return jdbc.queryForList("SELECT video_id FROM " + schema + ".playlist_videos WHERE playlist_id = ? ORDER BY video_id",
                UUID.class, playlistId).stream().sorted().toList();
    }
}
