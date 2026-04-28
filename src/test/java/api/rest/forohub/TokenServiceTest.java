package api.rest.forohub;

import api.rest.forohub.domain.usuario.Usuario;
import api.rest.forohub.infra.exceptions.TokenInvalidoException;
import api.rest.forohub.infra.security.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

class TokenServiceTest {

    private TokenService tokenService;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        tokenService = new TokenService();
        ReflectionTestUtils.setField(tokenService, "KEY_SECRET", "clave-secreta-de-prueba-muy-larga-para-test");

        usuario = new Usuario();
        ReflectionTestUtils.setField(usuario, "login", "testuser");
    }

    @Test
    @DisplayName("Debe generar un access token válido")
    void generarToken_debeRetornarTokenValido() {
        String token = tokenService.generarToken(usuario);
        assertThat(token).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("Debe validar correctamente un access token")
    void validarToken_conTokenValido_debeRetornarSubject() {
        String token = tokenService.generarToken(usuario);
        String subject = tokenService.validarToken(token);
        assertThat(subject).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Debe lanzar excepción con token inválido")
    void validarToken_conTokenInvalido_debeLanzarExcepcion() {
        assertThatThrownBy(() -> tokenService.validarToken("token.invalido.123"))
                .isInstanceOf(TokenInvalidoException.class);
    }

    @Test
    @DisplayName("Debe generar un refresh token válido")
    void generarRefreshToken_debeRetornarTokenValido() {
        String token = tokenService.generarRefreshToken(usuario);
        assertThat(token).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("No debe validar access token como refresh token")
    void validarRefreshToken_conAccessToken_debeLanzarExcepcion() {
        String accessToken = tokenService.generarToken(usuario);
        assertThatThrownBy(() -> tokenService.validarRefreshToken(accessToken))
                .isInstanceOf(TokenInvalidoException.class);
    }
}