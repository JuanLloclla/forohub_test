package api.rest.forohub.domain.auth;

import api.rest.forohub.domain.auth.dto.DatosLogin;
import api.rest.forohub.domain.auth.dto.DatosRegister;
import api.rest.forohub.domain.auth.dto.DatosTokenJWT;
import api.rest.forohub.domain.usuario.Usuario;
import api.rest.forohub.domain.usuario.UsuarioRepository;
import api.rest.forohub.infra.exceptions.RecursoDuplicadoException;
import api.rest.forohub.infra.exceptions.TokenInvalidoException;
import api.rest.forohub.infra.security.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public DatosTokenJWT login(DatosLogin datos){
        var authToken = new UsernamePasswordAuthenticationToken(datos.login(), datos.contrasena());
        var auth = authenticationManager.authenticate(authToken);
        var usuario = (Usuario) auth.getPrincipal();

        var accessToken = tokenService.generarToken(usuario);
        var refreshToken = tokenService.generarRefreshToken(usuario);
        var refresh = new RefreshToken(refreshToken, usuario);
        refreshTokenRepository.save(refresh);

        return new DatosTokenJWT(accessToken, refreshToken);
    }

    @Transactional
    public DatosTokenJWT register(DatosRegister datos){
        if (usuarioRepository.existsByLogin(datos.login())){
            throw new RecursoDuplicadoException("El usuario ya existe");
        }
        var usuario = new Usuario(datos, passwordEncoder);
        usuarioRepository.save(usuario);

        var accessToken = tokenService.generarToken(usuario);
        var refreshToken = tokenService.generarRefreshToken(usuario);
        var refresh = new RefreshToken(refreshToken, usuario);
        refreshTokenRepository.save(refresh);

        return  new DatosTokenJWT(accessToken, refreshToken);
    }

    @Transactional
    public DatosTokenJWT refresh(String tokenViejo) {
        // Validamos el token refresh con JWT
        var subject = tokenService.validarRefreshToken(tokenViejo);
        // Validamos el token refresh en la base de datos
        var refreshTokenAntiguo = refreshTokenRepository.findByToken(tokenViejo)
                .orElseThrow(() -> new TokenInvalidoException("Refresh token inválido"));
        // Validamos que el token refresh sea del mismo usuario
        if (!refreshTokenAntiguo.getUsuario().getLogin().equals(subject)){
            throw new TokenInvalidoException("Token no coincide con el usuario");
        }
        // Validamos si el token esta expirado
        if (refreshTokenAntiguo.estaExpirado()){
            refreshTokenRepository.deleteByToken(tokenViejo);
            throw new TokenInvalidoException("Refresh token expirado, inicia sesión nuevamente");
        }
        var usuario = refreshTokenAntiguo.getUsuario();
        refreshTokenRepository.deleteByToken(tokenViejo);

        var accessToken = tokenService.generarToken(usuario);
        var refreshToken = tokenService.generarRefreshToken(usuario);
        var refresh = new RefreshToken(refreshToken, usuario);
        refreshTokenRepository.save(refresh);

        return new DatosTokenJWT(accessToken, refreshToken);
    }

    @Transactional
    public void logout(String token) {
        // Validar JWT (firma, expiración, tipo refresh)
        try {
            tokenService.validarRefreshToken(token);
        } catch (Exception e) {
            return;
        }
        // Validar en la base de datos
        refreshTokenRepository.findByToken(token)
                // eliminarlo si el token esta presente
                .ifPresent(refreshToken -> refreshTokenRepository.delete(refreshToken));
    }
}
