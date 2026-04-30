package api.rest.forohub;

import api.rest.forohub.domain.auth.CookieService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("CookieService - Pruebas Unitarias")
class CookieServiceTest {

    private CookieService cookieService;

    @BeforeEach
    void setUp() {
        cookieService = new CookieService();
    }

    @Test
    @DisplayName("createRefreshCookie debe retornar cookie con nombre refreshToken")
    void createRefreshCookie_debe_tener_nombre_correcto() {
        var cookie = cookieService.createRefreshCookie("mi-token");

        assertThat(cookie.getName()).isEqualTo("refreshToken");
        assertThat(cookie.getValue()).isEqualTo("mi-token");
    }

    @Test
    @DisplayName("createRefreshCookie debe ser HttpOnly y Secure")
    void createRefreshCookie_debe_ser_httpOnly_y_secure() {
        var cookie = cookieService.createRefreshCookie("mi-token");

        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.isSecure()).isTrue();
    }

    @Test
    @DisplayName("createRefreshCookie debe expirar en 30 días")
    void createRefreshCookie_debe_expirar_en_30_dias() {
        var cookie = cookieService.createRefreshCookie("mi-token");

        assertThat(cookie.getMaxAge().toDays()).isEqualTo(30);
    }

    @Test
    @DisplayName("deleteRefreshCookie debe retornar cookie con maxAge 0")
    void deleteRefreshCookie_debe_tener_maxAge_cero() {
        var cookie = cookieService.deleteRefreshCookie();

        assertThat(cookie.getName()).isEqualTo("refreshToken");
        assertThat(cookie.getMaxAge().toSeconds()).isEqualTo(0);
    }
}