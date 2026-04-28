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
class ValidadorTopicoDuplicadoTest {

    @Mock
    private TopicoRepository topicoRepository;

    @InjectMocks
    private ValidadorTopicoDuplicado validador;

    @Test
    @DisplayName("Debe lanzar excepción cuando el tópico ya existe")
    void validar_conTopicoDuplicado_debeLanzarExcepcion() {
        var datos = new DatosRegistroTopico("Titulo repetido", "Mensaje repetido", "Curso");
        when(topicoRepository.existsByTituloAndMensaje("Titulo repetido", "Mensaje repetido"))
                .thenReturn(true);

        assertThatThrownBy(() -> validador.validar(datos))
                .isInstanceOf(RecursoDuplicadoException.class)
                .hasMessageContaining("Ya existe un tópico");
    }

    @Test
    @DisplayName("No debe lanzar excepción cuando el tópico es nuevo")
    void validar_conTopicoNuevo_noDebeLanzarExcepcion() {
        var datos = new DatosRegistroTopico("Titulo nuevo", "Mensaje nuevo", "Curso");
        when(topicoRepository.existsByTituloAndMensaje("Titulo nuevo", "Mensaje nuevo"))
                .thenReturn(false);

        assertThatNoException().isThrownBy(() -> validador.validar(datos));
    }
}