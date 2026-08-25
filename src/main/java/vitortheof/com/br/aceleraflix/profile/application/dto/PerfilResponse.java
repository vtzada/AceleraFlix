package vitortheof.com.br.aceleraflix.profile.application.dto;

public record PerfilResponse(
        String nome,
        String username,
        String bio,
        String avatarUrl,
        String githubUrl,
        String linkedinUrl,
        boolean verificado
) {
}
