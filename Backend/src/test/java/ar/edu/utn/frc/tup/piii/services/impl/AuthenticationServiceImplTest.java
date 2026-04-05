package ar.edu.utn.frc.tup.piii.services.impl;

import ar.edu.utn.frc.tup.piii.model.entities.User;
import ar.edu.utn.frc.tup.piii.model.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AuthenticationServiceImplTest {

    private UserRepository userRepository;
    private AuthenticationServiceImpl authenticationService;

    @BeforeEach
    public void setUp() {
        userRepository = mock(UserRepository.class);
        authenticationService = new AuthenticationServiceImpl(userRepository);

        ReflectionTestUtils.setField(authenticationService, "jwtSecret", "claveSecreta");
        ReflectionTestUtils.setField(authenticationService, "jwtExpiration", 3600000L);
    }

    @Test
    public void testLoginSuccessfully() {
        User user = new User();
        user.setEmail("test@correo.com");
        user.setPassword(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("1234"));
        user.setId(1);
        user.setName("Usuario");

        when(userRepository.findByEmail("test@correo.com")).thenReturn(user);

        String token = authenticationService.login("test@correo.com", "1234");

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    public void loginUserInvalidTest() {
        when(userRepository.findByEmail("test@correo.com")).thenReturn(null);

        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                authenticationService.login("test@correo.com", "1234"));

        assertEquals("Usuario no encontrado", ex.getMessage());
    }

    @Test
    public void loginPasswordInvalidTest() {
        User user = new User();
        user.setEmail("test@correo.com");
        user.setPassword(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("correcta"));

        when(userRepository.findByEmail("test@correo.com")).thenReturn(user);

        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                authenticationService.login("test@correo.com", "incorrecta"));

        assertEquals("Contraseña incorrecta", ex.getMessage());
    }

    @Test
    public void makeTokenTest() {
        User user = new User();
        user.setEmail("test@correo.com");
        user.setId(5);
        user.setName("Usuario");

        String token = authenticationService.generarToken(user);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }
}
