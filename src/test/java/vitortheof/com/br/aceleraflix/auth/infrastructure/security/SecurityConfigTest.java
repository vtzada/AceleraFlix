package vitortheof.com.br.aceleraflix.auth.infrastructure.security;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringJUnitConfig(SecurityConfigTest.TestConfig.class)
@WebAppConfiguration
@TestPropertySource(properties = { "jwt.secret=test-only-secret", "jwt.expiration-access=1000", "jwt.expiration-refresh=2000" })
class SecurityConfigTest {
    @Autowired WebApplicationContext context;
    @Autowired JwtProvider jwt;
    @Autowired CustomUserDetailsService users;
    MockMvc mvc;
    UUID userId = UUID.randomUUID();

    @BeforeEach
    void setup() {
        reset(jwt, users);
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        when(jwt.validarTokenERetornarUsuarioId("expired", "access"))
                .thenThrow(new TokenInvalidoException("Token expirado"));
        when(jwt.validarTokenERetornarUsuarioId("valid", "access")).thenReturn(userId);
        when(users.loadUserById(userId)).thenReturn(User.withUsername("usuario").password("unused").roles("USUARIO").build());
    }

    @Test void missingTokenReturns401() throws Exception {
        mvc.perform(post("/playlists/liked/video")).andExpect(status().isUnauthorized());
    }
    @Test void expiredTokenReturns401ForFrontendRefresh() throws Exception {
        mvc.perform(post("/playlists/liked/video").header("Authorization", "Bearer expired"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(users);
    }
    @Test void normalUserCanAccessPlaylistActions() throws Exception {
        mvc.perform(post("/playlists/liked/video").header("Authorization", "Bearer valid"))
                .andExpect(status().isOk());
    }
    @Test void authenticatedUserWithoutAdminRoleStillReceives403() throws Exception {
        mvc.perform(post("/admin-only").header("Authorization", "Bearer valid"))
                .andExpect(status().isForbidden());
    }

    @Configuration
    @EnableWebMvc
    @EnableWebSecurity
    @Import(SecurityConfig.class)
    static class TestConfig {
        @Bean JwtProvider jwt() { return mock(JwtProvider.class); }
        @Bean CustomUserDetailsService users() { return mock(CustomUserDetailsService.class); }
        @Bean SecurityFilter filter(JwtProvider jwt, CustomUserDetailsService users) { return new SecurityFilter(jwt, users); }
        @Bean TestController controller() { return new TestController(); }
    }
    @RestController
    static class TestController {
        @PostMapping("/playlists/liked/video") String like() { return "ok"; }
        @PreAuthorize("hasRole('ADMIN')")
        @PostMapping("/admin-only") String admin() { return "ok"; }
    }
}
