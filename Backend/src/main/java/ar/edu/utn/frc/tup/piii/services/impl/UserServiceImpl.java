package ar.edu.utn.frc.tup.piii.services.impl;

import ar.edu.utn.frc.tup.piii.model.entities.Role;
import ar.edu.utn.frc.tup.piii.model.entities.User;
import ar.edu.utn.frc.tup.piii.model.repository.RoleRepository;
import ar.edu.utn.frc.tup.piii.model.repository.UserRepository;
import ar.edu.utn.frc.tup.piii.services.interfaces.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementación del servicio de usuarios.
 * Gestiona el registro, recuperación y administración de usuarios.
 */



@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final RoleRepository roleRepository;

    public UserServiceImpl(UserRepository repo, RoleRepository roleRepository) {
        this.userRepository = repo;
        this.bCryptPasswordEncoder = new BCryptPasswordEncoder();
        this.roleRepository = roleRepository;
    }

    @Override
    public User save(User user) {
        // Verifica que no esté registrado el email
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email ya registrado");
        }
        // Verifica el formato del email
        if (!user.getEmail().contains("@")) {
            throw new IllegalArgumentException("Email no válido");
        }
        // Valida longitud email
        if (user.getEmail().length() < 10 || user.getEmail().length() > 30) {
            throw new IllegalArgumentException("El email debe tener entre 10 y 30 caracteres");
        }
        // Verifica que el nombre no exista
        if (userRepository.existsByName(user.getName())) {
            throw new IllegalArgumentException("Nombre ya registrado");
        }
        // Valida longitud nombre
        if (user.getName().length() < 3 || user.getName().length() > 20) {
            throw new IllegalArgumentException("El nombre debe tener entre 3 y 20 caracteres");
        }
        // Verifica que se ingrese una contraseña
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }
        // Verifica longitud de la contraseña
        if (user.getPassword().length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 carácteres");
        }
        // Asigna rol número 2 (jugador) si no viene seteado
        if (user.getRole() == null) {
            Role deffaultRole = roleRepository.findById(2)
                    .orElseThrow(() -> new IllegalArgumentException("Rol por defecto no encontrado"));
            user.setRole(deffaultRole);
        }
        // Encripta la contraseña antes de guardarla
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        // Guarda el usuario y lo retorna
        return userRepository.save(user);
    }

    @Override
    public User update(Long id, User user) {
        // Verifica que el usuario exista por ID
        User original = userRepository.findById(Math.toIntExact(id))
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        // Si no se ingresa email, se mantiene el original
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            user.setEmail(original.getEmail());
        } else {
            // Valida longitud email
            if (user.getEmail().length() < 10 || user.getEmail().length() > 30) {
                throw new IllegalArgumentException("El email debe tener entre 10 y 30 caracteres");
            }
            // Valida que el nuevo email no exista
            if (!user.getEmail().equals(original.getEmail()) &&
                    userRepository.existsByEmail(user.getEmail())) {
                throw new IllegalArgumentException("Email ya registrado por otro usuario");
            }
        }
        // Si no se ingresa nombre, se mantiene el original
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(original.getName());
        } else {
            // Valida longitud nombre
            if (user.getName().length() < 3 || user.getName().length() > 20) {
                throw new IllegalArgumentException("El nombre debe tener entre 3 y 20 caracteres");
            }
            // Valida si quiere cambiar el nombre y que no exista
            if (!user.getName().equals(original.getName()) &&
                    userRepository.existsByName(user.getName())) {
                throw new IllegalArgumentException("Nombre ya registrado");
            }
        }
        // Validacion de contraseña
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            user.setPassword(original.getPassword());
        } else if (user.getPassword().length() < 6) {
            throw new IllegalArgumentException("La nueva contraseña debe tener al menos 6 caracteres");
        } else {
            user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        }
        // Si no se ingresa rol, se mantiene el original
        if (user.getRole() == null) {
            user.setRole(original.getRole());
        }
        // Asegura mantener el ID
        user.setId(original.getId());

        return userRepository.save(user);
    }


    @Override
    public User findById(int id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User findByEmail(String name) {
        return userRepository.findByEmail(name);
    }

    @Override
    public User findByName(String name) {
        return userRepository.findByName(name);
    }

}
