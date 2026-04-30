package api.rest.forohub;

import api.rest.forohub.domain.auth.AuthService;
import api.rest.forohub.domain.auth.RefreshToken;
import api.rest.forohub.domain.auth.RefreshTokenRepository;
import api.rest.forohub.domain.auth.dto.DatosRegister;
import api.rest.forohub.domain.usuario.Usuario;
import api.rest.forohub.domain.usuario.UsuarioRepository;
import api.rest.forohub.infra.exceptions.RecursoDuplicadoException;
import api.rest.forohub.infra.exceptions.TokenInvalidoException;
import api.rest.forohub.infra.security.TokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService - Pruebas Unitarias")
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenService tokenService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private Usuario crearUsuario() {
        var usuario = new Usuario();
        ReflectionTestUtils.setField(usuario, "id", 1L);
        ReflectionTestUtils.setField(usuario, "login", "testuser");
        return usuario;
    }

    // ─── register ───

    @Test
    @DisplayName("Register con usuario nuevo debe retornar tokens")
    void register_usuario_nuevo_retorna_tokens() {
        var datos = new DatosRegister("testuser", "password123");
        var usuario = crearUsuario();

        when(usuarioRepository.existsByLogin("testuser")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(usuarioRepository.save(any())).thenReturn(usuario);
        when(tokenService.generarToken(any())).thenReturn("access-token");
        when(tokenService.generarRefreshToken(any())).thenReturn("refresh-token");

        var resultado = authService.register(datos);

        assertThat(resultado.accessToken()).isEqualTo("access-token");
        assertThat(resultado.refreshToken()).isEqualTo("refresh-token");
    }

    @Test
    @DisplayName("Register con usuario ya existente debe lanzar RecursoDuplicadoException")
    void register_usuario_existente_lanza_excepcion() {
        var datos = new DatosRegister("testuser", "password123");

        when(usuarioRepository.existsByLogin("testuser")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(datos))
                .isInstanceOf(RecursoDuplicadoException.class)
                .hasMessage("El usuario ya existe");
    }

    // ─── refresh ───

    @Test
    @DisplayName("Refresh con token válido debe retornar nuevos tokens")
    void refresh_token_valido_retorna_nuevos_tokens() {
        var usuario = crearUsuario();
        var refreshToken = new RefreshToken("token-viejo", usuario);

        when(tokenService.validarRefreshToken("token-viejo")).thenReturn("testuser");
        when(refreshTokenRepository.findByToken("token-viejo")).thenReturn(Optional.of(refreshToken));
        when(tokenService.generarToken(any())).thenReturn("nuevo-access-token");
        when(tokenService.generarRefreshToken(any())).thenReturn("nuevo-refresh-token");

        var resultado = authService.refresh("token-viejo");

        assertThat(resultado.accessToken()).isEqualTo("nuevo-access-token");
        assertThat(resultado.refreshToken()).isEqualTo("nuevo-refresh-token");
    }

    @Test
    @DisplayName("Refresh con token no encontrado en BD debe lanzar TokenInvalidoException")
    void refresh_token_no_encontrado_lanza_excepcion() {
        when(tokenService.validarRefreshToken("token-falso")).thenReturn("testuser");
        when(refreshTokenRepository.findByToken("token-falso")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh("token-falso"))
                .isInstanceOf(TokenInvalidoException.class)
                .hasMessage("Refresh token inválido");
    }

    @Test
    @DisplayName("Refresh con token expirado debe lanzar TokenInvalidoException")
    void refresh_token_expirado_lanza_excepcion() {
        var usuario = crearUsuario();
        var refreshToken = new RefreshToken("token-expirado", usuario);
        ReflectionTestUtils.setField(refreshToken, "expiracion",
                java.time.LocalDateTime.now().minusDays(1));

        when(tokenService.validarRefreshToken("token-expirado")).thenReturn("testuser");
        when(refreshTokenRepository.findByToken("token-expirado")).thenReturn(Optional.of(refreshToken));

        assertThatThrownBy(() -> authService.refresh("token-expirado"))
                .isInstanceOf(TokenInvalidoException.class)
                .hasMessage("Refresh token expirado, inicia sesión nuevamente");
    }

    // ─── logout ───

    @Test
    @DisplayName("Logout con token válido debe eliminar el refresh token")
    void logout_token_valido_elimina_refresh_token() {
        var usuario = crearUsuario();
        var refreshToken = new RefreshToken("token-valido", usuario);

        when(tokenService.validarRefreshToken("token-valido")).thenReturn("testuser");
        when(refreshTokenRepository.findByToken("token-valido")).thenReturn(Optional.of(refreshToken));

        authService.logout("token-valido");

        verify(refreshTokenRepository, times(1)).delete(refreshToken);
    }

    @Test
    @DisplayName("Logout con token inválido no debe lanzar excepción")
    void logout_token_invalido_no_lanza_excepcion() {
        when(tokenService.validarRefreshToken("token-invalido"))
                .thenThrow(new TokenInvalidoException("invalido"));

        assertThatNoException().isThrownBy(() -> authService.logout("token-invalido"));
    }
}