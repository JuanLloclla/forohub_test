package api.rest.forohub.domain.auth;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class CookieService {

    public ResponseCookie createRefreshCookie(String token){
        return ResponseCookie.from("refreshToken", token)
                .httpOnly(true)
                .secure(true)
                .path("/auth")
                .sameSite("None") // backend y frontend tienen distintos dominio
                .maxAge(Duration.ofDays(30))
                .build();
    }

    public ResponseCookie deleteRefreshCookie(){
        return ResponseCookie.from("refreshToken","")
                .httpOnly(true)
                .secure(true)
                .path("/auth")
                .sameSite("None")
                .maxAge(0) // Expira inmediatamente
                .build();
    }
}
