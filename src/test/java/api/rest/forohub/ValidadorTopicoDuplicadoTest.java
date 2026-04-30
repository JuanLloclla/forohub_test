package api.rest.forohub;

import api.rest.forohub.domain.topicos.TopicoRepository;
import api.rest.forohub.domain.topicos.dto.DatosRegistroTopico;
import api.rest.forohub.domain.topicos.validacion.ValidadorTopicoDuplicado;
import api.rest.forohub.infra.exceptions.RecursoDuplicadoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ValidadorTopicoDuplicado - Pruebas Unitarias")
class ValidadorTopicoDuplicadoTest {

    @Mock
    private TopicoRepository topicoRepository;

    @InjectMocks
    private ValidadorTopicoDuplicado validador;

    @Test
    @DisplayName("Dado un título y mensaje únicos, no debe lanzar excepción")
    void validar_conTopicoNuevo_noDebeLanzarExcepcion() {
        var datos = new DatosRegistroTopico("Titulo nuevo", "Mensaje nuevo", "Curso");
        when(topicoRepository.existsByTituloAndMensaje("Titulo nuevo", "Mensaje nuevo"))
                .thenReturn(false);

        assertThatNoException().isThrownBy(() -> validador.validar(datos));
    }

    @Test
    @DisplayName("Dado un título y mensaje ya existentes, debe lanzar RecursoDuplicadoException")
    void validar_conTopicoDuplicado_debeLanzarExcepcion() {
        var datos = new DatosRegistroTopico("Titulo repetido", "Mensaje repetido", "Curso");
        when(topicoRepository.existsByTituloAndMensaje("Titulo repetido", "Mensaje repetido"))
                .thenReturn(true);

        assertThatThrownBy(() -> validador.validar(datos))
                .isInstanceOf(RecursoDuplicadoException.class);
    }

    @Test
    @DisplayName("Debe consultar el repositorio exactamente una vez por invocación")
    void validar_debeConsultarRepositorioExactamenteUnaVez() {
        var datos = new DatosRegistroTopico("Titulo", "Mensaje", "Curso");
        when(topicoRepository.existsByTituloAndMensaje("Titulo", "Mensaje"))
                .thenReturn(false);

        validador.validar(datos);

        verify(topicoRepository, times(1))
                .existsByTituloAndMensaje("Titulo", "Mensaje");
    }

    @Test
    @DisplayName("La excepción debe contener el mensaje de negocio correcto")
    void validar_excepcionDebeContenerMensajeDeNegocio() {
        var datos = new DatosRegistroTopico("Titulo repetido", "Mensaje repetido", "Curso");
        when(topicoRepository.existsByTituloAndMensaje("Titulo repetido", "Mensaje repetido"))
                .thenReturn(true);

        assertThatThrownBy(() -> validador.validar(datos))
                .isInstanceOf(RecursoDuplicadoException.class)
                .hasMessage("Ya existe un tópico con el mismo titulo y mensaje");
    }
}