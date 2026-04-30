package api.rest.forohub;

import api.rest.forohub.domain.auth.AuthService;
import api.rest.forohub.domain.auth.CookieService;
import api.rest.forohub.domain.auth.dto.DatosLogin;
import api.rest.forohub.domain.auth.dto.DatosRegister;
import api.rest.forohub.domain.auth.dto.DatosTokenJWT;
import api.rest.forohub.infra.exceptions.RecursoDuplicadoException;
import api.rest.forohub.infra.exceptions.TokenInvalidoException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("AuthController - Pruebas de Integración")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private CookieService cookieService;

    private ResponseCookie cookieFalsa() {
        return ResponseCookie.from("refreshToken", "refresh-token-falso")
                .httpOnly(true)
                .secure(true)
                .path("/auth")
                .maxAge(Duration.ofDays(30))
                .build();
    }

    private ResponseCookie cookieVacia() {
        return ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/auth")
                .maxAge(0)
                .build();
    }

    // ─── POST /auth/login ───

    @Test
    @DisplayName("POST /auth/login con credenciales válidas debe retornar 200 con accessToken")
    void login_credenciales_validas_retorna_200() throws Exception {
        var datos = new DatosLogin("testuser", "password123");
        var tokens = new DatosTokenJWT("access-token", "refresh-token");

        when(authService.login(any())).thenReturn(tokens);
        when(cookieService.createRefreshCookie(any())).thenReturn(cookieFalsa());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datos)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"));
    }

    @Test
    @DisplayName("POST /auth/login con body inválido debe retornar 400")
    void login_body_invalido_retorna_400() throws Exception {
        var datosInvalidos = new DatosLogin("", "");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datosInvalidos)))
                .andExpect(status().isBadRequest());
    }

    // ─── POST /auth/register ───

    @Test
    @DisplayName("POST /auth/register con datos válidos debe retornar 201")
    void register_datos_validos_retorna_201() throws Exception {
        var datos = new DatosRegister("newuser", "password123");
        var tokens = new DatosTokenJWT("access-token", "refresh-token");

        when(authService.register(any())).thenReturn(tokens);
        when(cookieService.createRefreshCookie(any())).thenReturn(cookieFalsa());

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datos)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("access-token"));
    }

    @Test
    @DisplayName("POST /auth/register con usuario duplicado debe retornar 409")
    void register_usuario_duplicado_retorna_409() throws Exception {
        var datos = new DatosRegister("existinguser", "password123");

        when(authService.register(any()))
                .thenThrow(new RecursoDuplicadoException("El usuario ya existe"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datos)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /auth/register con body inválido debe retornar 400")
    void register_body_invalido_retorna_400() throws Exception {
        var datosInvalidos = new DatosRegister("", "");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datosInvalidos)))
                .andExpect(status().isBadRequest());
    }

    // ─── POST /auth/refresh ───

    @Test
    @DisplayName("POST /auth/refresh con cookie válida debe retornar 200")
    void refresh_cookie_valida_retorna_200() throws Exception {
        var tokens = new DatosTokenJWT("nuevo-access-token", "nuevo-refresh-token");

        when(authService.refresh(any())).thenReturn(tokens);
        when(cookieService.createRefreshCookie(any())).thenReturn(cookieFalsa());

        mockMvc.perform(post("/auth/refresh")
                        .cookie(new jakarta.servlet.http.Cookie("refreshToken", "refresh-token-valido")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("nuevo-access-token"));
    }

    @Test
    @DisplayName("POST /auth/refresh sin cookie debe retornar 401")
    void refresh_sin_cookie_retorna_401() throws Exception {
        when(authService.refresh(null))
                .thenThrow(new TokenInvalidoException("Refresh token inválido"));

        mockMvc.perform(post("/auth/refresh"))
                .andExpect(status().isUnauthorized());
    }

    // ─── POST /auth/logout ───

    @Test
    @DisplayName("POST /auth/logout debe retornar 204")
    void logout_retorna_204() throws Exception {
        doNothing().when(authService).logout(any());
        when(cookieService.deleteRefreshCookie()).thenReturn(cookieVacia());

        mockMvc.perform(post("/auth/logout")
                        .cookie(new jakarta.servlet.http.Cookie("refreshToken", "token-valido")))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("POST /auth/logout debe eliminar la cookie")
    void logout_debe_eliminar_cookie() throws Exception {
        doNothing().when(authService).logout(any());
        when(cookieService.deleteRefreshCookie()).thenReturn(cookieVacia());

        mockMvc.perform(post("/auth/logout")
                        .cookie(new jakarta.servlet.http.Cookie("refreshToken", "token-valido")))
                .andExpect(status().isNoContent())
                .andExpect(header().exists("Set-Cookie"));
    }
}