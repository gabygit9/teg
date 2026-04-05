package ar.edu.utn.frc.tup.piii.configs;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SecurityConfig.class)
@TestPropertySource(properties = "jwt.secret=clave-de-prueba")
class SecurityConfigTest {

    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        TestPropertyValues.of("jwt.secret=clavePrueba123").applyTo(context);
        context.register(SecurityConfig.class);
        context.refresh();

        securityConfig = context.getBean(SecurityConfig.class);
    }

    @Test
    void testJwtFilter_validToken_shouldAuthenticate() throws ServletException, IOException {
        // Arrange
        String email = "test@example.com";
        String secret = "clavePrueba123";
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        String jwt = Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 100000))
                .signWith(SignatureAlgorithm.HS512, keyBytes)
                .compact();

        HttpServletRequest request = new MockHttpServletRequest() {{
            addHeader("Authorization", "Bearer " + jwt);
        }};
        HttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = Mockito.mock(FilterChain.class);

        // Act
        securityConfig.jwtAuthFilter().doFilter(request, response, chain);

        // Assert
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(email, ((org.springframework.security.core.userdetails.User)
                SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername());
    }

    @Test
    void testJwtFilter_invalidToken_shouldNotAuthenticate() throws ServletException, IOException {
        // Arrange
        HttpServletRequest request = new MockHttpServletRequest() {{
            addHeader("Authorization", "Bearer tokenInvalido");
        }};
        HttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = Mockito.mock(FilterChain.class);

        // Act
        securityConfig.jwtAuthFilter().doFilter(request, response, chain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, ((MockHttpServletResponse) response).getStatus());
    }

}