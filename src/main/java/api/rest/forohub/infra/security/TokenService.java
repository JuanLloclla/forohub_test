package api.rest.forohub.infra.security;

import api.rest.forohub.domain.usuario.Usuario;
import api.rest.forohub.infra.exceptions.TokenInvalidoException;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class TokenService {

    @Value("${api.security.token.secret}")
    private String KEY_SECRET;

    // Access token - 2 horas
    public String generarToken(Usuario usuario){
        try {
            var algorithm = Algorithm.HMAC256(KEY_SECRET);
            return JWT.create()
                    .withIssuer("Foro Hub API")
                    .withSubject(usuario.getLogin())
                    .withExpiresAt(fechaExpiracionAccessToken())
                    .sign(algorithm);
        } catch (JWTCreationException exception){
            throw new RuntimeException("Error al generar el token JWT", exception);
        }
    }

    // Valida el access token
    public String validarToken(String tokenJWT){
        try {
            var algoritmo = Algorithm.HMAC256(KEY_SECRET);
            return JWT.require(algoritmo)
                    .withIssuer("Foro Hub API")
                    .build()
                    .verify(tokenJWT)
                    .getSubject();
        } catch (JWTVerificationException exception){
            throw new TokenInvalidoException("Token JWT inválido o expirado!");
        }
    }

    // Refresh token - 30 dias
    public String generarRefreshToken(Usuario usuario){
        try {
            var algorithm = Algorithm.HMAC256(KEY_SECRET);
            return JWT.create()
                    .withIssuer("Foro Hub API")
                    .withSubject(usuario.getLogin())
                    .withClaim("tipo", "refresh")
                    .withExpiresAt(fechaExpiracionRefreshToken())
                    .sign(algorithm);
        } catch (JWTCreationException exception){
            throw new RuntimeException("Error al generar el refresh token JWT", exception);
        }
    }

    // Valida que sea un refresh token y no un access token
    public String validarRefreshToken(String tokenJWT){
        try {
            var algoritmo = Algorithm.HMAC256(KEY_SECRET);
            return JWT.require(algoritmo)
                    .withIssuer("Foro Hub API")
                    .withClaim("tipo", "refresh") // ← verifica que sea refresh token
                    .build()
                    .verify(tokenJWT)
                    .getSubject();
        } catch (JWTVerificationException exception){
            throw new TokenInvalidoException("Refresh token inválido o expirado!");
        }
    }

    private Instant fechaExpiracionAccessToken(){
        return Instant.now().plus(Duration.ofHours(2));
    }

    private Instant fechaExpiracionRefreshToken() {
        return Instant.now().plus(Duration.ofDays(30));
    }

}
