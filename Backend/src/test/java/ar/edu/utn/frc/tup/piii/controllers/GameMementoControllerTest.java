package ar.edu.utn.frc.tup.piii.controllers;

import ar.edu.utn.frc.tup.piii.dto.SaveGameRequestDTO;
import ar.edu.utn.frc.tup.piii.dto.mementosDTOs.GameStateMementoDTO;
import ar.edu.utn.frc.tup.piii.model.entities.Game;
import ar.edu.utn.frc.tup.piii.model.entities.GameMemento;
import ar.edu.utn.frc.tup.piii.services.interfaces.GameMementoService;
import ar.edu.utn.frc.tup.piii.model.repository.GameRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GameMementoController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class GameMementoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GameMementoService gameMementoService;

    @MockitoBean
    private GameRepository gameRepository;

    @Test
    void testGetLastState_OK() throws Exception {
        GameMemento memento = new GameMemento();
        when(gameMementoService.getLastState(1)).thenReturn(memento);

        mockMvc.perform(get("/api/v1/mementos/game/1/last"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetLastState_NotFound() throws Exception {
        when(gameMementoService.getLastState(1)).thenReturn(null);

        mockMvc.perform(get("/api/v1/mementos/game/1/last"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testSaveMemento_OK() throws Exception {
        Game game = new Game();
        GameMemento memento = new GameMemento();
        when(gameRepository.findById(1)).thenReturn(Optional.of(game));
        when(gameMementoService.saveMementoComplete(game, 1)).thenReturn(memento);

        mockMvc.perform(post("/api/v1/mementos/game/1/save?version=1"))
                .andExpect(status().isOk());
    }

    @Test
    void testSaveMemento_NotFound() throws Exception {
        when(gameRepository.findById(1)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/mementos/game/1/save?version=1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testRestoreState_OK() throws Exception {
        GameStateMementoDTO dto = new GameStateMementoDTO();
        when(gameMementoService.restoreAndPersistState(1)).thenReturn(dto);

        mockMvc.perform(post("/api/v1/mementos/restore/1"))
                .andExpect(status().isOk());
    }

    @Test
    void testRestoreState_BadRequest() throws Exception {
        when(gameMementoService.restoreAndPersistState(1)).thenThrow(RuntimeException.class);

        mockMvc.perform(post("/api/v1/mementos/restore/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testListSaveGames_OK() throws Exception {
        when(gameMementoService.listSaveGames()).thenReturn(List.of(new SaveGameRequestDTO()));

        mockMvc.perform(get("/api/v1/mementos"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}
