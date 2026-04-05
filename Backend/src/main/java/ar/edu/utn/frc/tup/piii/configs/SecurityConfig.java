package ar.edu.utn.frc.tup.piii.configs;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebSecurity // Habilita la configuración de seguridad web en Spring
@Profile("!test")
public class SecurityConfig {

    @Value("${jwt.secret}") // Inyecta el valor de la clave secreta desde properties
    private String jwtSecret;

    // Configuración de los filtros de seguridad. Todos son obligatorios para que funcione Spring Security y JWT, así que más o menos
    // sigue una plantilla de cómo suele ser, no hay nada extra
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http
                    // Deshabilita protección CSRF (es al vicio ponerla para el uso que le daríamos)
                    .cors(cors -> {})
                    .csrf(AbstractHttpConfigurer::disable)
                    .headers(headers -> headers
                            .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                    )
                    // Acá se ponen los endpoints que NO van a requerir autentificación para ingresar. Seguramente haya que ir agregando
                    // o cambiando si se cambian las rutas de los endpoints
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/h2-console/**").permitAll()
                            .requestMatchers("/api/v1/users/login").permitAll()
                            .requestMatchers("/api/v1/users/register").permitAll()
                            .requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()
                            // El resto de los endpoints, requieren autentificación
                            .anyRequest().authenticated()
                    )
                    .sessionManagement(session -> session
                            .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Necesario para JWT, ya que guarda la información en el token
                            // y no en la petición HTTP.
                    )
                    // Antes de pedir el usuario y contraseña, veficia que haya un JWT creado. Si lo hay, entonces no te vuelve a soliciar
                    // las credenciales. De no hacer ésto, te pediría usuario y contraseña cada vez que cambiar de página.
                    .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class);

            return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    // Spring Security validando las credenciales
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) {
        try {
            return authConfig.getAuthenticationManager();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo crear AuthenticationManager", e);
        }
    }
    //Define el filtro personalizado que se ejecuta una vez por cada petición
    @Bean
    public OncePerRequestFilter jwtAuthFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(
                    HttpServletRequest request,
                    HttpServletResponse response,
                    FilterChain filterChain
            ) throws ServletException, IOException {
                String header = request.getHeader("Authorization");
                //Verifica que el header no sea nulo y que comience con Baarer
                if (header != null && header.startsWith("Bearer ")) {
                    // (El token generado cuando el usuario se autentifica tiene el formado: "Bearer eyJhbGciOiJIUz...")
                    // asi que extrae el token JWT sin Bearer
                    String token = header.substring(7);
                    try {
                        // A la hora de recibir el token, hace la misma conversion que hacia el metodo "GenerarToken"
                        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
                        SecretKeySpec key = new SecretKeySpec(keyBytes, SignatureAlgorithm.HS512.getJcaName());

                        //Parsea el token JWT usando la clave secreta para validar la firma
                        Claims claims = Jwts.parser()
                                .setSigningKey(key)
                                .parseClaimsJws(token)
                                .getBody();
                        // Obtiene el email del token
                        String email = claims.getSubject();
                        // Si el token es valido y no hay un usuario autenticado
                        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                            // Crea un objeto de autenticación con el email extraído del token
                            UsernamePasswordAuthenticationToken auth =
                                    new UsernamePasswordAuthenticationToken(
                                            new User(email, "", Collections.emptyList()),
                                            null,
                                            Collections.emptyList()
                                    );
                            // Se setean detalles de la petición y se establece la autenticación
                            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(auth);
                        }
                    } catch (Exception e) {
                        // Token invalido o expirado
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        return;
                    }
                }
                filterChain.doFilter(request, response);
            }
        };
    }

    // manejo de cors
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*")); // acepta todos los orígenes
        configuration.setAllowedMethods(List.of("*")); // todos los metodos aceptados
        configuration.setAllowedHeaders(List.of("*")); // acepta cualquier header
        configuration.setAllowCredentials(true); // permite tokens

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // aplica a todos los endpoints de los controllers
        return source;
    }


    // PD: Intenté factorizar todo sin "lambdas" (por la polémica que ha habido respecto a usar algo que no vimos) pero tuve muchos problemas
    // puesto que desde la versión 6 de Spring Security, está todo diseñado para que se escriba parecido a como está puesto ahora
    // (En realidad separando todo en métodos más pequeños pero bueno), entonces de cada forma que intenté, siempre me marcó que algo estaba "deprecated"
    //
}
