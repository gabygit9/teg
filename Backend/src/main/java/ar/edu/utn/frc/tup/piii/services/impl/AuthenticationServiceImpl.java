package ar.edu.utn.frc.tup.piii.services.impl;

import ar.edu.utn.frc.tup.piii.model.entities.User;
import ar.edu.utn.frc.tup.piii.model.repository.UserRepository;
import ar.edu.utn.frc.tup.piii.services.interfaces.AuthenticationService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Implementación del servicio de autenticación.
 * Maneja el inicio de sesión, registro y validación de credenciales.
 *
 *  @author GabrielaCamacho
 *  @version 1.0
 */
@Service
public class AuthenticationServiceImpl implements AuthenticationService {


    private final UserRepository userRepo;
    // Utilidad para encriptar y verificar contraseñas
    private final BCryptPasswordEncoder passwordEncoder;

    // Firma secreta del JWT, está en "properties"
    @Value("${jwt.secret}")
    private String jwtSecret;

    // El tiempo de expiración del JWT también configurado en application.properties
    @Value("${jwt.expiracion}")
    private long jwtExpiration;

    public AuthenticationServiceImpl(UserRepository repo) {
        this.userRepo = repo;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }


    // Autentica un usuario y genera un token JWT si las credenciales son correctas
    @Override
    public String login(String email, String password){
        // Verifica que el email exista
        User user = userRepo.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("Usuario no encontrado");
        }

        // Verifica si la contraseña ingresada coincide
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Contraseña incorrecta");
        }

        // Si las credenciales son válidas, genera un token JWT
        return generarToken(user);
    }

    // Genera un token JWT para el usuario autenticado.
    public String generarToken(User user) {
        // Transforma la firma del JTW a bit
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        // Crea una clave secreta compatible con HS512
        SecretKeySpec key = new SecretKeySpec(keyBytes, SignatureAlgorithm.HS512.getJcaName());

        return Jwts.builder()
                // Obtiene el email
                .setSubject(user.getEmail())
                .claim("id", user.getId())
                .claim("nombre", user.getName())
                // Obtiene la fecha y hora actual
                .setIssuedAt(new Date(System.currentTimeMillis()))
                // Obtiene la fecha y hora de la expiracion del token
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                // Firma el token usando la clave que se generó
                .signWith(SignatureAlgorithm.HS512, key)
                .compact();
    }
}
