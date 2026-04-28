package api.rest.forohub.domain.auth.dto;

public record DatosTokenJWT(
        String accessToken,
        String refreshToken
) {
}
