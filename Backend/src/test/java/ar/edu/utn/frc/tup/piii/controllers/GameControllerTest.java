package ar.edu.utn.frc.tup.piii.controllers;

import ar.edu.utn.frc.tup.piii.dto.*;
import ar.edu.utn.frc.tup.piii.model.entities.*;
import ar.edu.utn.frc.tup.piii.services.interfaces.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameControllerTest {

    @Mock private GameService gameService;
    @Mock private TurnService turnService;
    @Mock private CombatService combatService;
    @Mock private CardService cardService;
    @Mock private PlayerService playerService;

    @InjectMocks private GameController controller;

    @Test
    void getById() {
        Game game = new Game();
        when(gameService.findById(1)).thenReturn(game);

        ResponseEntity<Game> resp = controller.getById(1);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
    }

    @Test
    void putArmies() {
        CollocationDto dto = new CollocationDto();
        dto.setPlayerGameId(1);
        dto.setCountryId(1);
        dto.setArmies(3);

        ResponseEntity<?> resp = controller.putArmies(dto);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
    }

    @Test
    void attack() {
        ResultAttackDto res = new ResultAttackDto();
        when(combatService.attack(anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(res);

        ResponseEntity<?> resp = controller.attack(new AttackDto());

        assertEquals(HttpStatus.OK, resp.getStatusCode());
    }

    @Test
    void regroup() {
        ResponseEntity<?> resp = controller.regroup(new RegroupArmyDto(1,2,3,1));
        assertEquals(HttpStatus.OK, resp.getStatusCode());
    }

    @Test
    void finishTurn() {
        // Arrange
        FinishTurnDto dto = new FinishTurnDto();
        dto.setTurnId(1); // debe coincidir con el ID del turno mockeado
        dto.setPlayerGameId(5);

        Turn turnMock = new Turn();
        turnMock.setId(1);

        when(turnService.findById(1)).thenReturn(turnMock);

        // Act
        ResponseEntity<Map<String, String>> resp = controller.finishTurn(dto);

        // Assert
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals("Turno finalizado correctamente", resp.getBody().get("message"));

        verify(turnService).finishTurn(turnMock);
        verify(turnService).finishTurnRound(5, turnMock);
    }


    @Test
    void getPlayer() {
        // Arrange
        int gameId = 1;
        List<PlayerGame> players = List.of(new PlayerGame(), new PlayerGame());
        when(gameService.getPlayerGame(gameId)).thenReturn(players);

        // Act
        ResponseEntity<List<PlayerGame>> resp = controller.getPlayer(gameId);

        // Assert
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(2, resp.getBody().size());
    }

    @Test
    void regroupArmies_success() {
        RegroupArmyDto dto = new RegroupArmyDto();
        dto.setPlayerId(1);
        dto.setOriginId(10);
        dto.setDestinationId(20);
        dto.setAmount(5);

        ResponseEntity<?> resp = controller.regroup(dto);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals("Reagrupamiento realizado correctamente", resp.getBody());
        verify(combatService).regroupArmy(1, 10, 20, 5);
    }

    @Test
    void attack_withoutConquer() {
        AttackDto dto = new AttackDto();
        dto.setGameId(1);
        dto.setCountryAttackerId(10);
        dto.setCountryDefenderId(20);
        dto.setPlayerGameId(5);
        dto.setDice(3); // ← esto es un int

        ResultAttackDto result = new ResultAttackDto();
        result.setWasConquest(false);

        when(combatService.attack(1, 10, 20, 3)).thenReturn(result);

        ResponseEntity<?> response = controller.attack(dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(result, response.getBody());

        verify(combatService).attack(1, 10, 20, 3);
        verifyNoInteractions(cardService);
    }
    @Test
    void putArmies_ok() {
        CollocationDto dto = new CollocationDto();
        dto.setPlayerGameId(1);
        dto.setCountryId(10);
        dto.setArmies(3);

        doNothing().when(turnService).putArmy(1, 10, 3);

        ResponseEntity<?> response = controller.putArmies(dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());

        verify(turnService).putArmy(1, 10, 3);
    }

    @Test
    void getPlayer_returnList() {
        int gameId = 1;
        List<PlayerGame> players = new ArrayList<>();
        players.add(new PlayerGame());

        when(gameService.getPlayerGame(gameId)).thenReturn(players);

        ResponseEntity<List<PlayerGame>> resp = controller.getPlayer(gameId);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(players, resp.getBody());
        verify(gameService).getPlayerGame(gameId);
    }


    @Test
    void getPlayer_emptyList_returnNoContent() {
        int gameId = 1;

        when(gameService.getPlayerGame(gameId)).thenReturn(Collections.emptyList());

        ResponseEntity<List<PlayerGame>> resp = controller.getPlayer(gameId);

        assertEquals(HttpStatus.NO_CONTENT, resp.getStatusCode());
        assertNull(resp.getBody());
        verify(gameService).getPlayerGame(gameId);
    }
    @Test
    void getById_doesntExists_returnNotFound() {
        int gameId = 1;

        when(gameService.findById(gameId)).thenReturn(null);

        ResponseEntity<Game> resp = controller.getById(gameId);

        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
        assertNull(resp.getBody());
        verify(gameService).findById(gameId);
    }
    @Test
    void getById_throwException_returnInternalServerError() {
        int gameId = 1;

        when(gameService.findById(gameId)).thenThrow(new RuntimeException("Falla"));

        ResponseEntity<Game> resp = controller.getById(gameId);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
        assertNull(resp.getBody());
        verify(gameService).findById(gameId);
    }
    @Test
    void finishGame_withError_returnInternalServerError() {
        int gameId = 1;
        doThrow(new RuntimeException("Error")).when(gameService).endGame(gameId);

        ResponseEntity<String> resp = controller.finishGame(gameId);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
        verify(gameService).endGame(gameId);
    }
    @Test
    void putArmies_throwException_returnBadRequest() {
        CollocationDto dto = new CollocationDto();
        dto.setPlayerGameId(1);
        dto.setCountryId(2);
        dto.setArmies(3);

        doThrow(new IllegalArgumentException("Error")).when(turnService)
                .putArmy(1, 2, 3);

        ResponseEntity<?> response = controller.putArmies(dto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().toString().contains("Error"));
        verify(turnService).putArmy(1, 2, 3);
    }
    @Test
    void regroup_throwException_returnBadRequest() {
        RegroupArmyDto dto = new RegroupArmyDto();
        dto.setPlayerId(1);
        dto.setOriginId(2);
        dto.setDestinationId(3);
        dto.setAmount(4);

        doThrow(new IllegalArgumentException("No se puede")).when(combatService)
                .regroupArmy(1, 2, 3, 4);

        ResponseEntity<?> response = controller.regroup(dto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().toString().contains("No se puede"));
        verify(combatService).regroupArmy(1, 2, 3, 4);
    }
    @Test
    void finishTurnNotFound_returnBadRequest() {
        FinishTurnDto dto = new FinishTurnDto();
        dto.setTurnId(1);
        dto.setPlayerGameId(5);

        when(turnService.findById(1)).thenReturn(null);

        ResponseEntity<Map<String, String>> response = controller.finishTurn(dto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("error").contains("Turno no encontrado"));
    }
    @Test
    void attack_withConquer_askCard() {
        AttackDto dto = new AttackDto();
        dto.setGameId(1);
        dto.setCountryAttackerId(10);
        dto.setCountryDefenderId(20);
        dto.setPlayerGameId(5);
        dto.setDice(3);

        ResultAttackDto result = new ResultAttackDto();
        result.setWasConquest(true);

        when(combatService.attack(1, 10, 20, 3)).thenReturn(result);

        ResponseEntity<?> response = controller.attack(dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(result, response.getBody());

        verify(combatService).attack(1, 10, 20,3);
        verify(cardService).askCard(5); // <- clave
    }
    @Test
    void attack_throwException_returnBadRequest() {
        AttackDto dto = new AttackDto();
        dto.setGameId(1);
        dto.setCountryAttackerId(10);
        dto.setCountryDefenderId(20);
        dto.setPlayerGameId(5);
        dto.setDice(4);

        doThrow(new IllegalArgumentException("Ataque inválido")).when(combatService)
                .attack(1, 10, 20, 4);

        ResponseEntity<?> response = controller.attack(dto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().toString().contains("Ataque inválido"));
    }

    @Test
    void finishGame_throwException() {
        doThrow(new RuntimeException("Error grave")).when(gameService).endGame(99);

        ResponseEntity<String> response = controller.finishGame(99);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(gameService).endGame(99);
    }


    @Test
    void getPlayerGame_notFound_return404() {
        when(playerService.getAPlayerInAGame(anyInt(), anyInt()))
                .thenReturn(Optional.empty());

        ResponseEntity<PlayerGameDto> response = controller.getPlayerGame(1, 2);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }
    @Test
    void conquerCountry_ok_return200() {
        PlayerGame player = new PlayerGame();
        int countryId = 5;

        ResponseEntity<Void> response = controller.conquerCountry(player, countryId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(combatService).conquerCountry(countryId, player);
    }


    @Test
    void communicationStyle_returnOk() {
        when(gameService.communicationStyle(1)).thenReturn("Verbal");

        ResponseEntity<String> response = controller.communicationStyle(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Verbal", response.getBody());
    }
    @Test
    void communicationStyle_responseBlank_returnBadRequest() {
        when(gameService.communicationStyle(1)).thenReturn("  ");

        ResponseEntity<String> response = controller.communicationStyle(1);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
    @Test
    void communicationStyle_exception_return500() {
        when(gameService.communicationStyle(1)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<String> response = controller.communicationStyle(1);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("", response.getBody());
    }

}
