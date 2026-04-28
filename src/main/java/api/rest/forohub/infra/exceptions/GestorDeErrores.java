package api.rest.forohub.infra.exceptions;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GestorDeErrores {

    // 404 cuando no existe entidad
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity gestionarError404(){
        return ResponseEntity.notFound().build();
    }

    // 400 errores de validacion @valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity gestionarError400(MethodArgumentNotValidException ex){
        var errores = ex.getFieldErrors();
        return ResponseEntity.badRequest().body(errores.stream().map(DatosErrorValidacion::new).toList());
    }

    // 409 error de duplicado
    @ExceptionHandler(RecursoDuplicadoException.class)
    public ResponseEntity gestionarRecursoDuplicado(RecursoDuplicadoException ex){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new DatosError(ex.getMessage()));
    }

    // 403 error de acceso denegado
    @ExceptionHandler(AccesoDenegadoException.class)
    public ResponseEntity gestionarAccesoDenegado(AccesoDenegadoException ex){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new DatosError(ex.getMessage()));
    }

    // 401 error de tokens invalidos o expirados
    @ExceptionHandler(TokenInvalidoException.class)
    public ResponseEntity gestionarTokenInvalido(TokenInvalidoException ex){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new DatosError(ex.getMessage()));
    }

    public record DatosErrorValidacion(String campo, String mensaje){
        public DatosErrorValidacion(FieldError error){
            this(
                    error.getField(), error.getDefaultMessage()
            );
        }
    }

    public record DatosError(String message) {}
}
