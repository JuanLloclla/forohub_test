package api.rest.forohub.domain.auth;

import api.rest.forohub.domain.usuario.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity(name = "RefreshToken")
@Table(name = "refresh_tokens")

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")

public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String token;
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
    private LocalDateTime expiracion;

    public RefreshToken(String refreshToken, Usuario usuario) {
        this.token = refreshToken;
        this.usuario = usuario;
        this.expiracion = LocalDateTime.now().plusDays(30);
    }

    public boolean estaExpirado() {
        return LocalDateTime.now().isAfter(this.expiracion);
    }
}
