package ar.edu.utn.frc.tup.piii.controllers;

import ar.edu.utn.frc.tup.piii.dto.CreatePlayerRequestDto;
import ar.edu.utn.frc.tup.piii.dto.BasePlayerDTO;
import ar.edu.utn.frc.tup.piii.dto.PlayerGameDto;
import ar.edu.utn.frc.tup.piii.model.entities.HumanPlayer;
import ar.edu.utn.frc.tup.piii.model.entities.PlayerGame;
import ar.edu.utn.frc.tup.piii.services.interfaces.PlayerService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PlayerController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class PlayerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlayerService playerService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAll_ShouldReturnListPlayers() throws Exception {
        BasePlayerDTO player = new BasePlayerDTO();
        player.setPlayerName("Juan");

        when(playerService.findAll()).thenReturn(List.of(player));

        mockMvc.perform(get("/api/v1/players"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].namePlayer").value("Juan"));
    }

    @Test
    void getById_ShouldReturnPlayer() throws Exception {
        BasePlayerDTO player = new BasePlayerDTO();
        player.setPlayerName("Pedro");
        when(playerService.findById(1)).thenReturn(Optional.of(player));

        mockMvc.perform(get("/api/v1/players/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.namePlayer").value("Pedro"));
    }

    @Test
    void registerPlayerBot_ShouldReturnPlayerGame() throws Exception {
        PlayerGameDto dto = new PlayerGameDto();
        when(playerService.createBotPlayerGame(anyInt(), anyInt(), anyInt())).thenReturn(dto);

        mockMvc.perform(post("/api/v1/players/player-game-bot/1")
                        .param("difficultId", "2")
                        .param("colorId", "3"))
                .andExpect(status().isOk());
    }

    @Test
    void assignHumanInGame() throws Exception {
        PlayerGameDto dto = new PlayerGameDto();
        when(playerService.assignHumanInGame(anyInt(), anyString(), anyInt())).thenReturn(dto);

        mockMvc.perform(post("/api/v1/players/player-game-human/1")
                        .param("name", "Ana")
                        .param("colorId", "5"))
                .andExpect(status().isOk());
    }

    @Test
    void registerHumanPlayer_shouldReturnCreated() throws Exception {
        CreatePlayerRequestDto request = new CreatePlayerRequestDto();
        request.setName("Luis");
        request.setAvailableArmies(10);
        request.setUser("ejemplo");
        request.setGameId(2);
        request.setColorId(3);

        PlayerGame playerGame = new PlayerGame();
        playerGame.setPlayer(new HumanPlayer());

        when(playerService.createHumanPlayerAndAssignToGame(any(), anyString(), anyInt(), anyInt())).thenReturn(playerGame);

        mockMvc.perform(post("/api/v1/players/humans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void createPlayer_shouldReturnCreated() throws Exception {
        BasePlayerDTO player = new BasePlayerDTO();
        player.setPlayerName("Carlos");
        HumanPlayer entity = new HumanPlayer();

        when(playerService.saveHumanPlayer(any())).thenReturn(entity);

        mockMvc.perform(post("/api/v1/players/load-player")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(player)))
                .andExpect(status().isCreated());
    }

    @Test
    void updatePlayerArmiesUpdated() throws Exception {
        BasePlayerDTO player = new BasePlayerDTO();
        player.setAvailableArmies(20);

        when(playerService.updateArmies(eq(1), anyInt())).thenReturn(player);

        mockMvc.perform(put("/api/v1/players/1/armies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(player)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableArmies").value(20));
    }

    @Test
    void shouldValidatePathVariableMapping() throws Exception {
        // Given
        int userId = 7;
        BasePlayerDTO player = new BasePlayerDTO(1, "Melon", 0);

        when(playerService.findBasePlayerPerUserId(userId))
                .thenReturn(player);

        //String body = objectMapper.writeValueAsString(player);

        // When & Then
        mockMvc.perform(get("/api/v1/players/base-per-user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(player.getId()));

    }

}
