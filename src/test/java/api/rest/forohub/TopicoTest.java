package api.rest.forohub;

import api.rest.forohub.domain.topicos.Status;
import api.rest.forohub.domain.topicos.Topico;
import api.rest.forohub.domain.topicos.dto.DatosActualizacionTopico;
import api.rest.forohub.domain.topicos.dto.DatosRegistroTopico;
import api.rest.forohub.domain.usuario.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Topico (entidad) - Pruebas Unitarias")
class TopicoTest {

    private Usuario crearUsuario() {
        var usuario = new Usuario();
        ReflectionTestUtils.setField(usuario, "login", "testuser");
        return usuario;
    }

    @Test
    @DisplayName("Debe crear un tópico con datos correctos")
    void crear_topico_con_datos_correctos() {
        var datos = new DatosRegistroTopico("Titulo", "Mensaje", "Curso");
        var usuario = crearUsuario();
        var topico = new Topico(datos, usuario);

        assertThat(topico.getTitulo()).isEqualTo("Titulo");
        assertThat(topico.getMensaje()).isEqualTo("Mensaje");
        assertThat(topico.getCurso()).isEqualTo("Curso");
        assertThat(topico.getAutor()).isEqualTo(usuario);
    }

    @Test
    @DisplayName("Debe actualizar datos del tópico modificando titulo, mensaje y curso")
    void actualizar_datos_topico() {
        var datos = new DatosRegistroTopico("Titulo", "Mensaje", "Curso");
        var usuario = crearUsuario();
        var topico = new Topico(datos, usuario);

        var actualizacion = new DatosActualizacionTopico("Nuevo Titulo", "Nuevo Mensaje", "Nuevo Curso");
        topico.actualizarDatos(actualizacion);

        assertThat(topico.getTitulo()).isEqualTo("Nuevo Titulo");
        assertThat(topico.getMensaje()).isEqualTo("Nuevo Mensaje");
        assertThat(topico.getCurso()).isEqualTo("Nuevo Curso");
    }

    @Test
    @DisplayName("El ID debe ser nulo antes de persistir en la base de datos")
    void id_debe_ser_nulo_antes_de_persistir() {
        var datos = new DatosRegistroTopico("Titulo", "Mensaje", "Curso");
        var usuario = crearUsuario();
        var topico = new Topico(datos, usuario);

        assertThat(topico.getId()).isNull();
    }

    @Test
    @DisplayName("Antes de persistir, fecha y status deben ser nulos")
    void fecha_y_status_nulos_antes_de_persistir() {
        var datos = new DatosRegistroTopico("Titulo", "Mensaje", "Curso");
        var usuario = crearUsuario();
        var topico = new Topico(datos, usuario);

        assertThat(topico.getFecha()).isNull();
        assertThat(topico.getStatus()).isNull();
    }

    @Test
    @DisplayName("Al persistir, debe establecer status ABIERTO y fecha no nula")
    void pre_persist_debe_establecer_status_y_fecha() {
        var datos = new DatosRegistroTopico("Titulo", "Mensaje", "Curso");
        var usuario = crearUsuario();
        var topico = new Topico(datos, usuario);

        topico.inicializarDatos();

        assertThat(topico.getStatus()).isEqualTo(Status.ABIERTO);
        assertThat(topico.getFecha()).isNotNull();
    }

    @Test
    @DisplayName("Al actualizar datos, el autor del tópico no debe cambiar")
    void actualizar_datos_no_debe_cambiar_autor() {
        var datos = new DatosRegistroTopico("Titulo", "Mensaje", "Curso");
        var usuario = crearUsuario();
        var topico = new Topico(datos, usuario);

        var actualizacion = new DatosActualizacionTopico("Nuevo Titulo", "Nuevo Mensaje", "Nuevo Curso");
        topico.actualizarDatos(actualizacion);

        assertThat(topico.getAutor()).isEqualTo(usuario);
        assertThat(topico.getAutor().getUsername()).isEqualTo("testuser");
    }
}