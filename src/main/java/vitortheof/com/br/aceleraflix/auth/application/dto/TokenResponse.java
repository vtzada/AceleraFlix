package vitortheof.com.br.aceleraflix.auth.application.dto;

public record TokenResponse(String accessToken,
                            String refreshToken) {
}
