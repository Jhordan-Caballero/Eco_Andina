package com.gestion.residuos.Eco_Andino.exception;

import com.gestion.residuos.Eco_Andino.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

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
        return new ErrorResponse("Datos inválidos", errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse cuerpoIlegible() {
        return new ErrorResponse("El cuerpo de la petición está vacío o no es un JSON válido");
    }
}
