package com.gestion.residuos.Eco_Andino.exception;

import com.gestion.residuos.Eco_Andino.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.io.UncheckedIOException;
import java.util.LinkedHashMap;
import java.util.Map;

/** Convierte las excepciones en respuestas JSON {"message": ...} que el frontend muestra al usuario. */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Cubre también al usuario inexistente: DaoAuthenticationProvider lo convierte en BadCredentialsException
    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse credencialesInvalidas() {
        return new ErrorResponse("Usuario o contraseña incorrectos");
    }

    @ExceptionHandler(DisabledException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse usuarioDesactivado() {
        return new ErrorResponse("El usuario está desactivado. Contacta al administrador");
    }

    @ExceptionHandler(DemasiadosIntentosException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public ErrorResponse demasiadosIntentos() {
        return new ErrorResponse("Demasiados intentos fallidos. Espera unos minutos antes de volver a intentarlo");
    }

    // Token válido de un usuario que ya no existe (ej. eliminado después de iniciar sesión)
    @ExceptionHandler(UsernameNotFoundException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse sesionInvalida() {
        return new ErrorResponse("La sesión ya no es válida. Inicia sesión nuevamente");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse validacion(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        // El primer error como mensaje principal: es lo que el frontend muestra en la notificación
        String mensaje = errors.isEmpty() ? "Datos inválidos" : errors.values().iterator().next();
        return new ErrorResponse(mensaje, errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse cuerpoIlegible() {
        return new ErrorResponse("El cuerpo de la petición está vacío o no es un JSON válido");
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse parteFaltante(MissingServletRequestPartException ex) {
        return new ErrorResponse("Falta la parte '" + ex.getRequestPartName() + "' de la petición");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public ErrorResponse archivoGrande() {
        return new ErrorResponse("El archivo supera el tamaño máximo permitido (10 MB)");
    }

    @ExceptionHandler({SolicitudInvalidaException.class, IllegalArgumentException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse solicitudInvalida(RuntimeException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse noEncontrado(RecursoNoEncontradoException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(ConflictoException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse conflicto(ConflictoException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    // Respaldo de las restricciones de la BD (ej. dos peticiones simultáneas con el mismo código)
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse integridadDatos(DataIntegrityViolationException ex) {
        log.warn("Violación de integridad: {}", ex.getMostSpecificCause().getMessage());
        return new ErrorResponse("El registro entra en conflicto con datos existentes");
    }

    // Parámetro de URL con formato inválido (fecha mal escrita, estado que no existe…)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse parametroInvalido(MethodArgumentTypeMismatchException ex) {
        return new ErrorResponse("El valor del parámetro '" + ex.getName() + "' no es válido");
    }

    // Cubre @PreAuthorize y las validaciones de permiso de los servicios: siempre una respuesta JSON clara
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse accesoDenegado() {
        return new ErrorResponse("No tienes permisos para realizar esta operación");
    }

    @ExceptionHandler(UncheckedIOException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse errorArchivo(UncheckedIOException ex) {
        // La causa técnica queda en el log; al cliente solo el mensaje
        log.error("Error de entrada/salida", ex);
        return new ErrorResponse(ex.getMessage());
    }
}
