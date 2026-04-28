package api.rest.forohub.domain.topicos.validacion;

import api.rest.forohub.domain.topicos.TopicoRepository;
import api.rest.forohub.domain.topicos.dto.DatosRegistroTopico;
import api.rest.forohub.infra.exceptions.RecursoDuplicadoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ValidadorTopicoDuplicado {

    @Autowired
    private TopicoRepository topicoRepository;

    public void validar(DatosRegistroTopico datos){
        var topicoduplicado = topicoRepository.existsByTituloAndMensaje(datos.titulo(), datos.mensaje());

        if (topicoduplicado){
            throw new RecursoDuplicadoException("Ya existe un tópico con el mismo titulo y mensaje");
        }
    }
}
