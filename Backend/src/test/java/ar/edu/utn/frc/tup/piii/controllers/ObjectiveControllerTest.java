package ar.edu.utn.frc.tup.piii.controllers;

import ar.edu.utn.frc.tup.piii.model.entities.Objective;
import ar.edu.utn.frc.tup.piii.model.entities.PlayerGame;
import ar.edu.utn.frc.tup.piii.services.interfaces.PlayerService;
import ar.edu.utn.frc.tup.piii.services.interfaces.ObjectiveService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ObjectiveController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class ObjectiveControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ObjectiveService objectiveService;

    @MockitoBean
    private PlayerService playerService;

    @Test
    @DisplayName("GET /api/v1/objectives - debe devolver lista de objetivos")
    void testGetAllObjectives_OK() throws Exception {
        Objective objective1 = new Objective(1, "Conquistar Asia");
        Objective objective2 = new Objective(2, "Eliminar jugador rojo");
        List<Objective> objectives = List.of(objective1, objective2);

        when(objectiveService.findAll()).thenReturn(objectives);

        mockMvc.perform(get("/api/v1/objectives"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].description").value("Conquistar Asia"))
                .andExpect(jsonPath("$[1].description").value("Eliminar jugador rojo"));
    }

    @Test
    @DisplayName("GET /api/v1/objetivos - debe devolver bad request si lista vacía")
    void testGetAllObjectives_Empty() throws Exception {
        when(objectiveService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/objectives"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /{gameId}/player/{playerGameId}/objective - debe devolver objetivo secreto")
    void testGetSecretObjective_OK() throws Exception {
        Objective obj = new Objective(5, "Conquistar África");
        PlayerGame player = new PlayerGame();
        player.setSecretObjective(obj);

        when(playerService.getAPlayerInAGame(1, 10)).thenReturn(Optional.of(player));

        mockMvc.perform(get("/api/v1/objectives/1/player/10/objective"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    @DisplayName("GET /{gameId}/player/{playerGameId}/objective - debe devolver 404 si no se encuentra")
    void testGetSecretObjective_NotFound() throws Exception {
        when(playerService.getAPlayerInAGame(1, 10)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/objectives/1/player/10/objective"))
                .andExpect(status().isNotFound());
    }
}
