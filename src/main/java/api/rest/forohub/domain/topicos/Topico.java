package api.rest.forohub.domain.topicos;

import api.rest.forohub.domain.topicos.dto.DatosActualizacionTopico;
import api.rest.forohub.domain.topicos.dto.DatosRegistroTopico;
import api.rest.forohub.domain.usuario.Usuario;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity(name = "Topico")
@Table(name = "topicos")

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")

public class Topico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String titulo;
    private String mensaje;
    private LocalDateTime fecha;
    @Enumerated(EnumType.STRING)
    private Status status;
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario autor;
    private String curso;

    @PrePersist
    public void inicializarDatos(){
        this.fecha = LocalDateTime.now();
        this.status = Status.ABIERTO;
    }

    public Topico(DatosRegistroTopico datos, Usuario autor) {
        this.titulo = datos.titulo();
        this.mensaje = datos.mensaje();
        this.autor = autor;
        this.curso = datos.curso();
    }

    public void actualizarDatos(@Valid DatosActualizacionTopico datos) {
        this.titulo = datos.titulo();
        this.mensaje = datos.mensaje();
        this.curso = datos.curso();
    }
}
