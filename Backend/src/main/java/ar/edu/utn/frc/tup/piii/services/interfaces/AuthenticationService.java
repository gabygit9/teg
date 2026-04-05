package ar.edu.utn.frc.tup.piii.services.interfaces;

/**
 * Servicio de autenticación del sistema.
 * Define las operaciones relacionadas con el inicio de sesión, registro y validación de usuarios.
 *
 * @author GabrielaCamacho
 * @version 1.0
 */
public interface AuthenticationService {

    String login(String email, String password);

}
