package ar.edu.utn.frc.tup.piii.services.interfaces;

import ar.edu.utn.frc.tup.piii.model.entities.User;

import java.util.List;

/**
 * Interfaz que define las operaciones relacionadas con la gestión de usuarios.
 * Un usuario puede iniciar sesión, registrarse, o actualizar su perfil.
 *
 * @author Ismael Ceballos
 */
public interface UserService {

    User save(User user);

    User update(Long id, User user);

    User findById(int id);

    List<User> findAll();

    User findByEmail(String name);

    User findByName(String name);
}
