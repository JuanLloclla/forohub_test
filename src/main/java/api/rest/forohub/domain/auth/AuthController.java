package api.rest.forohub.domain.auth;

import api.rest.forohub.domain.auth.dto.DatosAccessToken;
import api.rest.forohub.domain.auth.dto.DatosLogin;
import api.rest.forohub.domain.auth.dto.DatosRegister;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(
        name = "Autenticación",
        description = """
                Gestión de sesiones de usuario mediante JWT.
                
                **Flujo de autenticación:**
                1. Regístrate en **/auth/register** o inicia sesión en **/auth/login**
                2. Recibirás un **accessToken** en el body (válido por 2 horas)
                3. El **refreshToken** se guarda automáticamente en una cookie HttpOnly (válido por 30 días)
                4. Usa el **accessToken** en el header **Authorization: Bearer <token>** para acceder a endpoints protegidos
                5. Cuando el **accessToken** expire, llama a **/auth/refresh** para obtener uno nuevo sin necesidad de iniciar sesión
                """
)
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private CookieService cookieService;

    @Operation(
            summary = "Iniciar sesión",
            description = """
                    Autentica al usuario y devuelve un `accessToken` en el body.
                    El `refreshToken` se establece automáticamente como cookie HttpOnly.
                    
                    **No es necesario** manejar el `refreshToken` manualmente, el navegador lo enviará solo.
                    """
    )
    @PostMapping("/login")
    public ResponseEntity<DatosAccessToken> iniciarSesion(@RequestBody @Valid DatosLogin datos){
        var tokens = authService.login(datos);
        var cookie = cookieService.createRefreshCookie(tokens.refreshToken());

        return ResponseEntity.ok()
                .header("Set-Cookie", cookie.toString())
                .body(new DatosAccessToken(tokens.accessToken()));
    }

    @Operation(
            summary = "Registrar nuevo usuario",
            description = """
                    Crea una nueva cuenta de usuario y devuelve tokens igual que `/auth/login`.
                    
                    El login debe ser único — si ya existe, se devuelve `409 Conflict`.
                    """
    )
    @PostMapping("/register")
    public ResponseEntity<DatosAccessToken> registrar(@RequestBody @Valid DatosRegister datos){
        var tokens = authService.register(datos);
        var cookie = cookieService.createRefreshCookie(tokens.refreshToken());

        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Set-Cookie", cookie.toString())
                .body(new DatosAccessToken(tokens.accessToken()));
    }

    @Operation(
            summary = "Renovar access token",
            description = """
                    Genera un nuevo par de tokens usando el `refreshToken` almacenado en la cookie.
                    
                    **Refresh token rotation:** el token anterior se invalida automáticamente.
                    Si el `refreshToken` no existe o expiró, deberás iniciar sesión nuevamente.
                    
                    *No se requiere enviar nada en el body — la cookie se envía automáticamente.*
                    """
    )
    @PostMapping("/refresh")
    public ResponseEntity<DatosAccessToken> refresh(
            @CookieValue(name = "refreshToken", required = false) @Parameter(hidden = true) String refreshToken){
        var tokens = authService.refresh(refreshToken);
        var cookie = cookieService.createRefreshCookie(tokens.refreshToken());

        return  ResponseEntity.ok()
                .header("Set-Cookie", cookie.toString())
                .body(new DatosAccessToken(tokens.accessToken()));
    }

    @Operation(
            summary = "Cerrar sesión",
            description = """
                    Invalida el `refreshToken` actual y elimina la cookie del navegador.
                    
                    El `accessToken` seguirá siendo técnicamente válido hasta su expiración (2 horas),
                    pero al no poder renovarse, la sesión queda efectivamente cerrada.
                    
                    *Esta operación es idempotente — si el token ya no existe, igual responde `204`.*
                    """
    )
    @PostMapping("/logout")
    public ResponseEntity<Void>logout(
            @CookieValue(name = "refreshToken", required = false) @Parameter(hidden = true) String refreshToken){
        authService.logout(refreshToken);
        var cookie = cookieService.deleteRefreshCookie();

        return ResponseEntity.noContent()
                .header("Set-Cookie", cookie.toString())
                .build();
    }

}
