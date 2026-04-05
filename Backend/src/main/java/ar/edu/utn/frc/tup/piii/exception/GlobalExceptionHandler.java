package ar.edu.utn.frc.tup.piii.exception;

import ar.edu.utn.frc.tup.piii.dtos.common.ErrorApi;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Manejador global de excepciones para toda la aplicación.
 *
 * Esta clase intercepta excepciones comunes y devuelve un objeto {@link ErrorApi}
 * con la información estructurada del error. Permite respuestas consistentes
 * en toda la API, facilitando el manejo de errores desde el cliente.
 *
 * Se captura:
 * - {@code NoSuchElementException} para recursos no encontrados (404)
 * - {@code MethodArgumentNotValidException} para errores de validación de entrada (400)
 * - {@code Exception} para cualquier otro error interno (500)
 *
 * @author GabrielaCamacho
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * Maneja excepciones por elementos no encontrados (ej: findById que devuelve Optional.empty()).
     *
     * @param ex excepción capturada.
     * @return respuesta HTTP 404 con estructura {@link ErrorApi}
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorApi> handleNotFound(NoSuchElementException ex) {
        String message = ex.getMessage() != null ? ex.getMessage() : "Elemento no encontrado";

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                buildError(HttpStatus.NOT_FOUND, message)
        );
    }

    /**
     * Maneja errores de validación cuando un DTO anotado con {@code @Valid} no cumple restricciones.
     *
     * @param ex excepción de validación.
     * @return respuesta HTTP 400 con lista de errores en el mensaje.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorApi> handleValidation(MethodArgumentNotValidException ex) {
        String errorMsg = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                buildError(HttpStatus.BAD_REQUEST, errorMsg)
        );
    }

    /**
     * Maneja cualquier excepción inesperada del sistema.
     *
     * @param ex excepción capturada.
     * @return respuesta HTTP 500 con mensaje genérico.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorApi> handleGeneral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor: " + ex.getMessage())
        );
    }

    /**
     * Construye un objeto {@link ErrorApi} con los datos del error.
     *
     * @param status código HTTP.
     * @param message mensaje descriptivo.
     * @return objeto de error.
     */
    private ErrorApi buildError(HttpStatus status, String message) {
        return ErrorApi.builder()
                .timestamp(LocalDateTime.now().toString())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .build();
    }
}
