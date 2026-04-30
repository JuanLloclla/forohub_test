package api.rest.forohub;

import api.rest.forohub.domain.auth.RefreshToken;
import api.rest.forohub.domain.usuario.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("RefreshToken - Pruebas Unitarias")
class RefreshTokenTest {

    private Usuario crearUsuario() {
        var usuario = new Usuario();
        ReflectionTestUtils.setField(usuario, "login", "testuser");
        return usuario;
    }

    @Test
    @DisplayName("estaExpirado debe retornar false cuando la expiración es futura")
    void estaExpirado_retorna_false_cuando_expiracion_es_futura() {
        var usuario = crearUsuario();
        var refreshToken = new RefreshToken("token123", usuario);

        assertThat(refreshToken.estaExpirado()).isFalse();
    }

    @Test
    @DisplayName("estaExpirado debe retornar true cuando la expiración ya pasó")
    void estaExpirado_retorna_true_cuando_expiracion_ya_paso() {
        var usuario = crearUsuario();
        var refreshToken = new RefreshToken("token123", usuario);
        ReflectionTestUtils.setField(refreshToken, "expiracion", LocalDateTime.now().minusDays(1));

        assertThat(refreshToken.estaExpirado()).isTrue();
    }
}