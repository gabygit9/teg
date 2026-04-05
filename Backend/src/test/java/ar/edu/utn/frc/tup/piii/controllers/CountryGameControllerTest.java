package ar.edu.utn.frc.tup.piii.controllers;

import ar.edu.utn.frc.tup.piii.dto.ArmyMovementsDto;
import ar.edu.utn.frc.tup.piii.dto.CountryGameDTO;
import ar.edu.utn.frc.tup.piii.dto.NeighborsRequestDto;
import ar.edu.utn.frc.tup.piii.mappers.CountryGameMapper;
import ar.edu.utn.frc.tup.piii.model.entities.Game;
import ar.edu.utn.frc.tup.piii.model.entities.PlayerGame;
import ar.edu.utn.frc.tup.piii.model.entities.CountryGame;
import ar.edu.utn.frc.tup.piii.model.entities.CountryGameId;
import ar.edu.utn.frc.tup.piii.services.interfaces.PlayerService;
import ar.edu.utn.frc.tup.piii.services.interfaces.CountryGameService;
import ar.edu.utn.frc.tup.piii.services.interfaces.GameService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CountryGameControllerTest {

    @Mock
    private CountryGameService countryGameService;
    @Mock
    private PlayerService playerService;
    @Mock
    private GameService gameService;

    @InjectMocks
    private CountryGameController controller;

    // --- obtenerMapaPartida ---

    @Test
    void getGameMap_notFound() {
        when(countryGameService.getCountriesOfGame(42)).thenReturn(Collections.emptyList());

        ResponseEntity<List<CountryGameDTO>> resp = controller.getGameMap(42);

        assertEquals(ResponseEntity.notFound().build().getStatusCode(), resp.getStatusCode());
        verify(countryGameService).getCountriesOfGame(42);
    }

    @Test
    void getGameMap_ok() {
        List<CountryGameDTO> dtos = List.of(new CountryGameDTO());
        when(countryGameService.getCountriesOfGame(7)).thenReturn(dtos);

        ResponseEntity<List<CountryGameDTO>> resp = controller.getGameMap(7);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertSame(dtos, resp.getBody());
    }

    // --- paisesPorJugador ---

    @Test
    void countriesPerPlayer_noContent() {
        when(countryGameService.findByGameAndPlayerGame(1, 2))
                .thenReturn(Collections.emptyList());

        ResponseEntity<List<CountryGameDTO>> resp = controller.countriesPerPlayer(1, 2);

        assertEquals(HttpStatus.NO_CONTENT, resp.getStatusCode());
    }

    @Test
    void countriesPerPlayer_ok() {
        List<CountryGame> entities = List.of(new CountryGame());
        List<CountryGameDTO> dtos = List.of(new CountryGameDTO());

        when(countryGameService.findByGameAndPlayerGame(10, 20))
                .thenReturn(entities);

        try (MockedStatic<CountryGameMapper> ms = mockStatic(CountryGameMapper.class)) {
            ms.when(() -> CountryGameMapper.toDtoList(entities)).thenReturn(dtos);

            ResponseEntity<List<CountryGameDTO>> resp = controller.countriesPerPlayer(10, 20);

            assertEquals(HttpStatus.OK, resp.getStatusCode());
            assertSame(dtos, resp.getBody());
        }
    }

    // --- reforzarPais ---

    @Test
    void strengthenCountry_ok() {
        when(countryGameService.increaseArmies(any(CountryGameId.class), eq(5)))
                .thenReturn(true);

        ResponseEntity<?> resp = controller.strengthenCountry(3, 4, 5);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        verify(countryGameService).increaseArmies(new CountryGameId(3, 4), 5);
    }

    @Test
    void strengthenCountry_notFound() {
        when(countryGameService.increaseArmies(any(), anyInt()))
                .thenReturn(false);

        ResponseEntity<?> resp = controller.strengthenCountry(8, 9, 1);

        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
    }

    // --- reducirEjercitos ---

    @Test
    void reduceArmies_ok() {
        ArmyMovementsDto dto = new ArmyMovementsDto();
        dto.setCountryId(2);
        dto.setGameId(3);
        dto.setAmount(7);

        when(countryGameService.reduceArmies(any(CountryGameId.class), eq(7)))
                .thenReturn(true);

        ResponseEntity<Void> resp = controller.reduceArmies(dto);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
    }

    @Test
    void reduceArmies_notFound() {
        ArmyMovementsDto dto = new ArmyMovementsDto();
        dto.setCountryId(2);
        dto.setGameId(3);
        dto.setAmount(7);

        when(countryGameService.reduceArmies(any(), anyInt()))
                .thenReturn(false);

        ResponseEntity<Void> resp = controller.reduceArmies(dto);

        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
    }

    // --- obtenerVecinosEnemigos ---

    @Test
    void getEnemyNeighbors_badRequest() {
        NeighborsRequestDto dto = new NeighborsRequestDto();
        dto.setGameId(1);
        dto.setPlayerGameId(99);
        dto.setCountryId(5);

        when(playerService.findByGameId(1)).thenReturn(Collections.emptyList());

        ResponseEntity<List<CountryGame>> resp = controller.getEnemyNeighbors(dto);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    @Test
    void getEnemyNeighbors_noContent() {
        NeighborsRequestDto dto = new NeighborsRequestDto();
        dto.setGameId(2);
        dto.setPlayerGameId(5);
        dto.setCountryId(7);

        PlayerGame jp = new PlayerGame();
        jp.setId(5);
        when(playerService.findByGameId(2)).thenReturn(List.of(jp));
        when(gameService.findById(2)).thenReturn(new Game());
        when(countryGameService.findEnemyNeighbors(7, jp, new Game()))
                .thenReturn(Collections.emptyList());

        ResponseEntity<List<CountryGame>> resp = controller.getEnemyNeighbors(dto);

        assertEquals(HttpStatus.NO_CONTENT, resp.getStatusCode());
    }

    @Test
    void getEnemyNeighbors_ok() {
        NeighborsRequestDto dto = new NeighborsRequestDto();
        dto.setGameId(3);
        dto.setPlayerGameId(42);
        dto.setCountryId(11);

        PlayerGame jp = new PlayerGame();
        jp.setId(42);
        Game p = new Game();

        when(playerService.findByGameId(3)).thenReturn(List.of(jp));
        when(gameService.findById(3)).thenReturn(p);

        List<CountryGame> neighbors = List.of(new CountryGame());
        when(countryGameService.findEnemyNeighbors(11, jp, p)).thenReturn(neighbors);

        ResponseEntity<List<CountryGame>> resp = controller.getEnemyNeighbors(dto);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertSame(neighbors, resp.getBody());
    }

    // --- repartirPaises ---

    @Test
    void distributeCountries_ok() {
        List<CountryGame> list = List.of(new CountryGame(), new CountryGame());
        when(countryGameService.distributeInitialCountries(77)).thenReturn(list);

        ResponseEntity<List<CountryGame>> resp = controller.distributeCountries(77);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertSame(list, resp.getBody());
    }
}
