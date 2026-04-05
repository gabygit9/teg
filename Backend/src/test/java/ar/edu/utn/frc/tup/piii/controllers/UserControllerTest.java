package ar.edu.utn.frc.tup.piii.controllers;

import ar.edu.utn.frc.tup.piii.model.entities.Role;
import ar.edu.utn.frc.tup.piii.model.entities.User;
import ar.edu.utn.frc.tup.piii.services.interfaces.AuthenticationService;
import ar.edu.utn.frc.tup.piii.services.interfaces.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthenticationService authenticationService;

    @Autowired
    private ObjectMapper objectMapper;

    private final Role roleAdmin = new Role(1, "ADMIN");

    @Test
    @DisplayName("POST /register - debería crear usuario correctamente")
    void testCreateUser_OK() throws Exception {
        User input = new User(0, "Juan", "juan@test.com", "1234", roleAdmin);
        User output = new User(1, "Juan", "juan@test.com", "1234", roleAdmin);

        when(userService.save(any(User.class))).thenReturn(output);

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Juan"))
                .andExpect(jsonPath("$.role.description").value("ADMIN"));
    }

    @Test
    @DisplayName("PUT /update/{id} - debería actualizar usuario")
    void testUpdateUser_OK() throws Exception {
        User update = new User(0, "Carlos", "carlos@test.com", "nuevaPass", roleAdmin);
        User updated = new User(1, "Carlos", "carlos@test.com", "nuevaPass", roleAdmin);

        when(userService.update(1L, update)).thenReturn(updated);

        mockMvc.perform(put("/api/v1/users/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Carlos"))
                .andExpect(jsonPath("$.role.description").value("ADMIN"));
    }

    @Test
    @DisplayName("POST /login - debería retornar token si login exitoso")
    void testLogin_OK() throws Exception {
        User credentials = new User(0, null, "admin@test.com", "admin", null);
        when(authenticationService.login("admin@test.com", "admin")).thenReturn("mocked.jwt.token");

        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(credentials)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mocked.jwt.token"));
    }

    @Test
    @DisplayName("GET / - debería retornar lista de usuarios")
    void testGetAll_OK() throws Exception {
        User u1 = new User(1, "Ana", "ana@test.com", "x", roleAdmin);
        User u2 = new User(2, "Beto", "beto@test.com", "y", roleAdmin);

        when(userService.findAll()).thenReturn(List.of(u1, u2));

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].role.description").value("ADMIN"));
    }

    @Test
    @DisplayName("GET /{id} - debería devolver usuario si existe")
    void testGetById_OK() throws Exception {
        User u = new User(1, "Zoe", "zoe@test.com", "secret", roleAdmin);

        when(userService.findById(1)).thenReturn(u);

        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Zoe"))
                .andExpect(jsonPath("$.role.description").value("ADMIN"));
    }

    @Test
    @DisplayName("GET /{id} - debería devolver 404 si no existe")
    void testGetById_NotFound() throws Exception {
        when(userService.findById(100)).thenReturn(null);

        mockMvc.perform(get("/api/v1/users/100"))
                .andExpect(status().isNotFound());
    }
}
