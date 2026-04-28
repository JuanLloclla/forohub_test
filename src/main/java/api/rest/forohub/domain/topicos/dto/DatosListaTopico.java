package api.rest.forohub.domain.topicos.dto;

import api.rest.forohub.domain.topicos.Status;
import api.rest.forohub.domain.topicos.Topico;

import java.time.LocalDateTime;

public record DatosListaTopico(
        Long id,
        String titulo,
        String mensaje,
        LocalDateTime fecha,
        Status status,
        String autor,
        String curso
) {
    public DatosListaTopico(Topico topico){
        this(
                topico.getId(),
                topico.getTitulo(),
                topico.getMensaje(),
                topico.getFecha(),
                topico.getStatus(),
                topico.getAutor().getUsername(),
                topico.getCurso()
        );
    }
}
