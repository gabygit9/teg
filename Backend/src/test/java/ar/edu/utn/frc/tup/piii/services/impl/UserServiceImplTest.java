package ar.edu.utn.frc.tup.piii.services.impl;

import ar.edu.utn.frc.tup.piii.model.entities.Role;
import ar.edu.utn.frc.tup.piii.model.entities.User;
import ar.edu.utn.frc.tup.piii.model.repository.RoleRepository;
import ar.edu.utn.frc.tup.piii.model.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void findByIdTest() {
        User user = new User();
        user.setId(1);
        user.setName("Usuario 1");

        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        User result = userService.findById(1);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Usuario 1", result.getName());
    }

    @Test
    void findAllTest() {
        User user1 = new User();
        user1.setId(1);
        user1.setName("Usuario 1");
        User user2 = new User();
        user2.setId(2);
        user2.setName("Usuario 2");

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        List<User> result = userService.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Usuario 1", result.get(0).getName());
        assertEquals("Usuario 2", result.get(1).getName());
    }

    @Test
    void findByEmailTest() {
        User user = new User();
        user.setId(1);
        user.setEmail("usuario@email.com");

        when(userRepository.findByEmail("usuario@email.com")).thenReturn(user);

        User result = userService.findByEmail("usuario@email.com");

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("usuario@email.com", result.getEmail());
    }

    @Test
    void findByNameTest() {
        User user = new User();
        user.setId(1);
        user.setName("Usuario 1");

        when(userRepository.findByName("Usuario 1")).thenReturn(user);

        User result = userService.findByName("Usuario 1");

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Usuario 1", result.getName());
    }

    @Test
    void saveUserExistsEmailTest() {
        User user = new User();
        user.setEmail("usuario@email.com");

        when(userRepository.existsByEmail("usuario@email.com")).thenReturn(true);

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> userService.save(user));

        assertEquals("Email ya registrado", exception.getMessage());
    }

    @Test
    void saveUserEmailFormatInvalidTest() {
        User user = new User();
        user.setEmail("usuarioemail.com");
        user.setName("Nombre");

        when(userRepository.existsByEmail("usuarioemail.com")).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.save(user));
        assertEquals("Email no válido", ex.getMessage());
    }

    @Test
    void saveUserEmailLengthInvalidTest() {
        User user = new User();
        user.setEmail("a@b.c");
        user.setName("Nombre");

        when(userRepository.existsByEmail("a@b.c")).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.save(user));
        assertEquals("El email debe tener entre 10 y 30 caracteres", ex.getMessage());
    }

    @Test
    void saveUserNameLengthInvalidTest() {
        User user = new User();
        user.setEmail("usuario@email.com");
        user.setName("No");

        when(userRepository.existsByEmail("usuario@email.com")).thenReturn(false);
        when(userRepository.existsByName("No")).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.save(user));
        assertEquals("El nombre debe tener entre 3 y 20 caracteres", ex.getMessage());
    }

    @Test
    void saveUserDoNotPasswordEnteredTest() {
        User user = new User();
        user.setEmail("usuario@email.com");
        user.setName("Usuario");
        user.setPassword("  ");

        when(userRepository.existsByEmail("usuario@email.com")).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.save(user));
        assertEquals("La contraseña es obligatoria", ex.getMessage());
    }


    @Test
    void saveUserRoleAutomaticTest() {
        User user = new User();
        user.setEmail("usuario@email.com");
        user.setPassword("123456");
        user.setName("Usuario");
        user.setRole(null);

        Role roleMock = new Role();
        roleMock.setId(2);
        roleMock.setDescription("Jugador");

        when(roleRepository.findById(2)).thenReturn(Optional.of(roleMock));
        when(userRepository.existsByEmail("usuario@email.com")).thenReturn(false);
        when(userRepository.save(user)).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.save(user);

        assertNotNull(result.getRole(), "El rol no fue asignado");
        assertEquals(2, result.getRole().getId());
        assertEquals("Jugador", result.getRole().getDescription());
    }

    @Test
    void saveUserEncryptPaswordTest() {
        User user = new User();
        user.setEmail("usuario@email.com");
        user.setName("UsuarioTest");
        user.setPassword("123456");

        Role role = new Role();
        role.setId(2);
        role.setDescription("Jugador");

        when(userRepository.existsByEmail("usuario@email.com")).thenReturn(false);
        when(roleRepository.findById(2)).thenReturn(Optional.of(role));
        when(userRepository.save(user)).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.save(user);

        assertNotEquals("123456", result.getPassword());
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        assertTrue(encoder.matches("123456", result.getPassword()));
    }


    @Test
    void saveUserSuccessTest() {
        User user = new User();
        user.setName("Usuario 1");
        user.setEmail("usuario@email.com");
        user.setPassword("123456");

        Role role = new Role();
        role.setId(2);
        role.setDescription("Jugador");

        when(userRepository.existsByEmail("usuario@email.com")).thenReturn(false);
        when(roleRepository.findById(2)).thenReturn(Optional.of(role));
        when(userRepository.save(user)).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.save(user);

        assertNotNull(result.getRole(), "El rol no fue asignado");
        assertEquals(2, result.getRole().getId());
        assertEquals("Jugador", result.getRole().getDescription());
        assertEquals("Usuario 1", result.getName());
        assertEquals("usuario@email.com", result.getEmail());
    }


    @Test
    void updateUserNotFoundTest() {
        User userUpdate = new User();
        userUpdate.setEmail("nuevo@email.com");
        userUpdate.setName("NuevoNombre");
        userUpdate.setPassword("123456");

        when(userRepository.findById(1)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.update(1L, userUpdate));

        assertEquals("Usuario no encontrado", ex.getMessage());
    }

    @Test
    void updateUserEmailLengthInvalidTest() {
        User original = new User();
        original.setId(1);
        original.setEmail("original@email.com");
        original.setName("Original");
        original.setPassword("encodedpassword");

        User update = new User();
        update.setEmail("corta@a");
        update.setName("Original");
        update.setPassword("123456");

        when(userRepository.findById(1)).thenReturn(Optional.of(original));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.update(1L, update));

        assertEquals("El email debe tener entre 10 y 30 caracteres", ex.getMessage());
    }

    @Test
    void updateUserEmailAlreadyExistsTest() {
        User original = new User();
        original.setId(1);
        original.setEmail("original@email.com");
        original.setName("Original");
        original.setPassword("encodedpassword");

        User update = new User();
        update.setEmail("nuevo@email.com");
        update.setName("Original");
        update.setPassword("123456");

        when(userRepository.findById(1)).thenReturn(Optional.of(original));
        when(userRepository.existsByEmail("nuevo@email.com")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.update(1L, update));

        assertEquals("Email ya registrado por otro usuario", ex.getMessage());
    }

    @Test
    void updateUserNameLengthInvalidTest() {
        User original = new User();
        original.setId(1);
        original.setEmail("original@email.com");
        original.setName("Original");
        original.setPassword("encodedpassword");

        User update = new User();
        update.setEmail("original@email.com");
        update.setName("No");
        update.setPassword("123456");

        when(userRepository.findById(1)).thenReturn(Optional.of(original));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.update(1L, update));

        assertEquals("El nombre debe tener entre 3 y 20 caracteres", ex.getMessage());
    }

    @Test
    void updateUserNameAlreadyExistsTest() {
        User original = new User();
        original.setId(1);
        original.setEmail("original@email.com");
        original.setName("Original");
        original.setPassword("encodedpassword");

        User update = new User();
        update.setEmail("original@email.com");
        update.setName("Existente");
        update.setPassword("123456");

        when(userRepository.findById(1)).thenReturn(Optional.of(original));
        when(userRepository.existsByName("Existente")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.update(1L, update));

        assertEquals("Nombre ya registrado", ex.getMessage());
    }

    @Test
    void updateUserWithoutChangeEmailAndNameTest() {
        User original = new User();
        original.setId(1);
        original.setEmail("original@email.com");
        original.setName("Original");
        original.setPassword("encodedpassword");
        original.setRole(new Role(2, "Jugador"));

        User update = new User();
        update.setEmail(null);
        update.setName(null);
        update.setPassword("");
        update.setRole(null);

        when(userRepository.findById(1)).thenReturn(Optional.of(original));
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.update(1L, update);

        assertEquals(original.getEmail(), result.getEmail());
        assertEquals(original.getName(), result.getName());
        assertEquals(original.getPassword(), result.getPassword());
        assertEquals(original.getRole(), result.getRole());
    }

    @Test
    void updateUserWithChangeSuccessfullyTest() {
        User original = new User();
        original.setId(1);
        original.setEmail("original@email.com");
        original.setName("Original");
        original.setPassword(new BCryptPasswordEncoder().encode("123456"));
        Role role = new Role(2, "Jugador");
        original.setRole(role);

        User update = new User();
        update.setEmail("nuevo@email.com");
        update.setName("NuevoNombre");
        update.setPassword("654321");
        update.setRole(null);

        when(userRepository.findById(1)).thenReturn(Optional.of(original));
        when(userRepository.existsByEmail("nuevo@email.com")).thenReturn(false);
        when(userRepository.existsByName("NuevoNombre")).thenReturn(false);
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.update(1L, update);

        assertEquals("nuevo@email.com", result.getEmail());
        assertEquals("NuevoNombre", result.getName());
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        assertTrue(encoder.matches("654321", result.getPassword()));
        assertEquals(role, result.getRole());
    }
}
