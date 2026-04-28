package api.rest.forohub.domain.topicos.dto;

import api.rest.forohub.domain.topicos.Topico;

import java.time.LocalDateTime;

public record DatosDetalleTopico(
        Long id,
        String titulo,
        String mensaje,
        LocalDateTime fecha,
        String autor
) {
    public DatosDetalleTopico(Topico topico) {
        this(
                topico.getId(),
                topico.getTitulo(),
                topico.getMensaje(),
                topico.getFecha(),
                topico.getAutor().getUsername()
        );
    }
}
