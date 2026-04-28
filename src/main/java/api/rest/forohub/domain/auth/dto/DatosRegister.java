package api.rest.forohub.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record DatosRegister(
        @NotBlank String login,
        @NotBlank String contrasena
) {
}
