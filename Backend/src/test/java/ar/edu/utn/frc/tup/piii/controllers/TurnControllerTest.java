package ar.edu.utn.frc.tup.piii.controllers;

import ar.edu.utn.frc.tup.piii.dto.AttackDto;
import ar.edu.utn.frc.tup.piii.dto.ResultAttackDto;
import ar.edu.utn.frc.tup.piii.dto.RegroupArmyDto;
import ar.edu.utn.frc.tup.piii.model.entities.Turn;
import ar.edu.utn.frc.tup.piii.services.interfaces.CombatService;
import ar.edu.utn.frc.tup.piii.services.interfaces.PlayerService;
import ar.edu.utn.frc.tup.piii.services.interfaces.GameService;
import ar.edu.utn.frc.tup.piii.services.interfaces.TurnService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TurnControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private TurnService turnService;

    @Mock
    private CombatService combatService;

    @Mock
    private PlayerService playerService;

    @Mock
    private GameService gameService;

    @InjectMocks
    private TurnController turnController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(turnController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void save_ShouldReturnCreated_WhenSaveSuccessfully() throws Exception {
        // Given
        Turn turn = new Turn();
        when(turnService.save(any(Turn.class))).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/api/v1/turns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(turn)))
                .andExpect(status().isCreated())
                .andExpect(content().string("true"));

        verify(turnService).save(any(Turn.class));
    }

    @Test
    void save_ShouldReturnBadRequest_WhenFailSaving() throws Exception {
        // Given
        Turn turn = new Turn();
        when(turnService.save(any(Turn.class))).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/api/v1/turns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(turn)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("false"));
    }

    @Test
    void save_ShouldReturnInternalServerError_WhenHappensException() throws Exception {
        // Given
        Turn turn = new Turn();
        when(turnService.save(any(Turn.class))).thenThrow(new RuntimeException("Error de base de datos"));

        // When & Then
        mockMvc.perform(post("/api/v1/turns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(turn)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("false"));
    }

    @Test
    void findById_ShouldReturnTurn_WhenExists() throws Exception {
        // Given
        int turnId = 1;
        Turn turn = new Turn();
        when(turnService.findById(turnId)).thenReturn(turn);

        // When & Then
        mockMvc.perform(get("/api/v1/turns/{id}", turnId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists());

        verify(turnService).findById(turnId);
    }

    @Test
    void findById_ShouldReturnBadRequest_WhenDoesntExists() throws Exception {
        // Given
        int turnId = 1;
        when(turnService.findById(turnId)).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/v1/turns/{id}", turnId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findById_ShouldReturnInternalServerError_WhenHappensException() throws Exception {
        // Given
        int turnId = 1;
        when(turnService.findById(turnId)).thenThrow(new RuntimeException("Error de base de datos"));

        // When & Then
        mockMvc.perform(get("/api/v1/turns/{id}", turnId))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void findAll_ShouldReturnListTurns_WhenExistsTurns() throws Exception {
        // Given
        List<Turn> turns = Arrays.asList(new Turn(), new Turn());
        when(turnService.findAll()).thenReturn(turns);

        // When & Then
        mockMvc.perform(get("/api/v1/turns"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));

        verify(turnService).findAll();
    }

    @Test
    void findAll_ShouldReturnNotFound_WhenDoesntExistTurns() throws Exception {
        // Given
        when(turnService.findAll()).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/v1/turns"))
                .andExpect(status().isNotFound());
    }

    @Test
    void startTurn_ShouldReturnInternalServerError_WhenPlayerNotFound() throws Exception {
        // Given
        int playerGameId = 999;
        int gameId = 1;

        when(playerService.findByGameId(gameId)).thenReturn(List.of());

        // When & Then
        mockMvc.perform(post("/api/v1/turns/start")
                        .param("playerGameId", String.valueOf(playerGameId))
                        .param("gameId", String.valueOf(gameId)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void putArmies_ShouldReturnOk_WhenIsCorrectlySet() throws Exception {
        // Given
        int playerId = 1;
        int countryId = 1;
        int quantity = 5;

        doNothing().when(turnService).putArmy(playerId, countryId, quantity);

        // When & Then
        mockMvc.perform(put("/api/v1/turns/{playerId}/put-armies", playerId)
                        .param("countryId", String.valueOf(countryId))
                        .param("quantity", String.valueOf(quantity)))
                .andExpect(status().isOk())
                .andExpect(content().string("Ejército colocado correctamente"));

        verify(turnService).putArmy(playerId, countryId, quantity);
    }

    @Test
    void putArmies_ShouldReturnBadRequest_WhenArgumentsInvalids() throws Exception {
        // Given
        int playerId = 1;
        int countryId = 1;
        int quantity = 5;

        doThrow(new IllegalArgumentException("País no válido"))
                .when(turnService).putArmy(playerId, countryId, quantity);

        // When & Then
        mockMvc.perform(put("/api/v1/turns/{playerId}/put-armies", playerId)
                        .param("countryId", String.valueOf(countryId))
                        .param("quantity", String.valueOf(quantity)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("País no válido"));
    }

    @Test
    void movePhaseCorrectly() throws Exception {
        // Given
        int turnId = 1;
        Turn turn = new Turn();
        when(turnService.findById(turnId)).thenReturn(turn);

        // When & Then
        mockMvc.perform(post("/api/v1/turns/{id}/move-phase", turnId))
                .andExpect(status().isOk());

        verify(turnService).movePhase(turn);
    }

    @Test
    void getAvailableActions_ShouldReturnActions_WhenTurnExists() throws Exception {
        // Given
        int turnId = 1;
        Turn turn = new Turn();
        List<String> actions = Arrays.asList("ATTACK", "REGROUP", "FINISH");

        when(turnService.findById(turnId)).thenReturn(turn);
        when(turnService.getAvailableActions(turn)).thenReturn(actions);

        // When & Then
        mockMvc.perform(get("/api/v1/turns/{id}/available-actions", turnId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0]").value("ATTACK"));
    }

    @Test
    void getAvailableActions_ShouldReturnNotFound_WhenTurnDoesntExists() throws Exception {
        // Given
        int turnId = 999;
        when(turnService.findById(turnId)).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/v1/turns/{id}/available-actions", turnId))
                .andExpect(status().isNotFound());
    }

    @Test
    void finishTurn_ShouldReturnOk_WhenFinishCorrectly() throws Exception {
        // Given
        int turnId = 1;
        Turn turn = new Turn();
        when(turnService.findById(turnId)).thenReturn(turn);

        // When & Then
        mockMvc.perform(post("/api/v1/turns/{id}/finish", turnId))
                .andExpect(status().isOk());

        verify(turnService).finishTurn(turn);
    }

    @Test
    void regroupArmies_ShouldReturnOk_WhenRegroupCorrectly() throws Exception {
        // Given
        RegroupArmyDto dto = new RegroupArmyDto();
        dto.setPlayerId(1);
        dto.setOriginId(1);
        dto.setDestinationId(2);
        dto.setAmount(3);

        doNothing().when(combatService)
                .regroupArmy(dto.getPlayerId(), dto.getOriginId(), dto.getDestinationId(), dto.getAmount());

        // When & Then
        mockMvc.perform(put("/api/v1/turns/regroup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Ejércitos reagrupados correctamente"));
    }

    @Test
    void regroupArmies_ShouldReturnBadRequest_WhenArgumentsInvalids() throws Exception {
        // Given
        RegroupArmyDto dto = new RegroupArmyDto();
        dto.setPlayerId(1);
        dto.setOriginId(1);
        dto.setDestinationId(2);
        dto.setAmount(3);

        doThrow(new IllegalArgumentException("Países no adyacentes"))
                .when(combatService)
                .regroupArmy(dto.getPlayerId(), dto.getOriginId(), dto.getDestinationId(), dto.getAmount());

        // When & Then
        mockMvc.perform(put("/api/v1/turns/regroup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Países no adyacentes"));
    }

    @Test
    void attackIsSuccessful() throws Exception {
        // Given
        AttackDto attackDto = new AttackDto();
        attackDto.setGameId(1);
        attackDto.setCountryAttackerId(1);
        attackDto.setCountryDefenderId(2);
        attackDto.setDice(3);

        ResultAttackDto result = new ResultAttackDto();
        // Configurar el result según tu implementación

        when(combatService.attack(
                attackDto.getGameId(),
                attackDto.getCountryAttackerId(),
                attackDto.getCountryDefenderId(),
                attackDto.getDice()
        )).thenReturn(result);

        // When & Then
        mockMvc.perform(post("/api/v1/turns/attack")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(attackDto)))
                .andExpect(status().isOk());

        verify(combatService).attack(
                attackDto.getGameId(),
                attackDto.getCountryAttackerId(),
                attackDto.getCountryDefenderId(),
                attackDto.getDice()
        );
    }

    @Test
    void attack_ShouldReturnBadRequest_WhenArgumentsInvalids() throws Exception {
        // Given
        AttackDto attackDto = new AttackDto();
        attackDto.setGameId(1);
        attackDto.setCountryAttackerId(1);
        attackDto.setCountryDefenderId(2);
        attackDto.setDice(3);

        when(combatService.attack(anyInt(), anyInt(), anyInt(), anyInt()))
                .thenThrow(new IllegalArgumentException("No puedes atacar tu propio país"));

        // When & Then
        mockMvc.perform(post("/api/v1/turns/attack")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(attackDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("No puedes atacar tu propio país"));
    }

    @Test
    void attack_ShouldReturnInternalServerError_WhenHappensException() throws Exception {
        // Given
        AttackDto attackDto = new AttackDto();
        attackDto.setGameId(1);
        attackDto.setCountryAttackerId(1);
        attackDto.setCountryDefenderId(2);
        attackDto.setDice(3);

        when(combatService.attack(anyInt(), anyInt(), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Error de base de datos"));

        // When & Then
        mockMvc.perform(post("/api/v1/turns/attack")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(attackDto)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Error interno: Error de base de datos"));
    }
}