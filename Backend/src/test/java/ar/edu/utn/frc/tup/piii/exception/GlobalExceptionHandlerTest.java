package ar.edu.utn.frc.tup.piii.exception;

import ar.edu.utn.frc.tup.piii.dtos.common.ErrorApi;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    @Test
    void handleNotFound() throws Exception {
        NoSuchElementException exception = new NoSuchElementException("Elemento no encontrado");

        ResponseEntity<ErrorApi> response = exceptionHandler.handleNotFound(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Elemento no encontrado", response.getBody().getMessage());

    }

    @Test
    void handleNotFound_withNullMessage() throws Exception {
        NoSuchElementException exception = new NoSuchElementException();

        ResponseEntity<ErrorApi> response = exceptionHandler.handleNotFound(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Elemento no encontrado", response.getBody().getMessage());

    }

    @Test
    void handleValidation() throws Exception{
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError1 = new FieldError("objectName", "fieldName1", "Field is required");
        FieldError fieldError2 = new FieldError("objectName", "fieldName2", "Invalid format");

        when(bindingResult.getFieldErrors()).thenReturn(Arrays.asList(fieldError1, fieldError2));

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ErrorApi> response = exceptionHandler.handleValidation(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());

        String expectedMessage = "fieldName1: Field is required; fieldName2: Invalid format";
        assertEquals(expectedMessage, response.getBody().getMessage()); // Ajusta según tu ErrorApi

    }

    @Test
    void handleGeneral() {
        Exception exception = new RuntimeException("");

        ResponseEntity<ErrorApi> response = exceptionHandler.handleGeneral(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("Error interno del servidor: ", response.getBody().getMessage());
    }

    @RestController     //fake controller --> xq no tenemos NoSuchElementException en el globalHandler. Tiene que estar para q se active el exception handler
    @RequestMapping("/api")
    static class TestController {
        @GetMapping("/test-not-found")
        String False_NoSuchElementException() {
            throw new NoSuchElementException();
        }
    }
}