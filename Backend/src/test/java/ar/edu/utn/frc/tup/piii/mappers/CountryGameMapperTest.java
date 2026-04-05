package ar.edu.utn.frc.tup.piii.mappers;

import ar.edu.utn.frc.tup.piii.dto.CountryGameDTO;
import ar.edu.utn.frc.tup.piii.model.entities.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CountryGameMapperTest {

    @Test
    @DisplayName("toDto debería mapear correctamente un PaisPartida no nulo")
    void testToDto() {
        Continent continent = new Continent();
        continent.setName("Europe");

        Country country = new Country();
        country.setName("France");
        country.setContinent(continent);

        BasePlayer player = new HumanPlayer();
        player.setName("Luis");

        Color color = new Color();
        color.setName("BLUE");

        PlayerGame playerGame = new PlayerGame();
        playerGame.setId(7);
        playerGame.setPlayer(player);
        playerGame.setColor(color);

        CountryGameId id = new CountryGameId();
        id.setCountryId(1);
        id.setGameId(10);

        CountryGame entity = new CountryGame();
        entity.setId(id);
        entity.setCountry(country);
        entity.setAmountArmies(3);
        entity.setPlayerGame(playerGame);

        // Act
        CountryGameDTO dto = CountryGameMapper.toDto(entity);

        // Assert
        assertEquals(1, dto.getCountryId());
        assertEquals(10, dto.getGameId());
        assertEquals("France", dto.getCountryName());
        assertEquals("Europe", dto.getContinent());
        assertEquals(3, dto.getAvailableArmies());
        assertEquals(7, dto.getPlayerId());
        assertEquals("Luis", dto.getPlayerName());
    }

    @Test
    @DisplayName("toDto debería retornar null si la entidad es null")
    void testToDto_NullInput() {
        assertNull(CountryGameMapper.toDto(null));
    }

    @Test
    @DisplayName("toEntity debería mapear correctamente un DTO no nulo")
    void testToEntity() {
        // Arrange
        CountryGameDTO dto = new CountryGameDTO(1, 10, "Francia", "Europa", 5, 7, "Luis", "AZUL");

        Country country = new Country();
        country.setId(1);
        country.setName("France");

        Game game = new Game();
        game.setId(10);

        PlayerGame player = new PlayerGame();
        player.setId(7);

        // Act
        CountryGame entity = CountryGameMapper.toEntity(dto, country, game, player);

        // Assert
        assertEquals(1, entity.getId().getCountryId());
        assertEquals(10, entity.getId().getGameId());
        assertEquals(country, entity.getCountry());
        assertEquals(game, entity.getGame());
        assertEquals(player, entity.getPlayerGame());
        assertEquals(5, entity.getAmountArmies());
    }

    @Test
    @DisplayName("toEntity debería retornar null si el DTO es null")
    void testToEntity_NullInput() {
        assertNull(CountryGameMapper.toEntity(null, new Country(), new Game(), new PlayerGame()));
    }

    @Test
    @DisplayName("toDtoList debería mapear correctamente una lista de entidades")
    void testToDtoList() {
        CountryGame entity = new CountryGame();
        CountryGameId id = new CountryGameId();
        id.setCountryId(1);
        id.setGameId(1);
        entity.setId(id);
        entity.setCountry(new Country());
        entity.setPlayerGame(new PlayerGame());
        entity.setAmountArmies(1);

        assertThrows(NullPointerException.class, () -> {
            List<CountryGameDTO> dtos = CountryGameMapper.toDtoList(List.of(entity));
        });

    }

    @Test
    @DisplayName("toDtoList debería retornar lista vacía si la entrada es null o vacía")
    void testToDtoList_EmptyOrNull() {
        assertTrue(CountryGameMapper.toDtoList(null).isEmpty());
        assertTrue(CountryGameMapper.toDtoList(Collections.emptyList()).isEmpty());
    }

    @Test
    @DisplayName("toEntityList debería mapear correctamente una lista de DTOs con mapas válidos")
    void testToEntityList() {
        CountryGameDTO dto = new CountryGameDTO(1, 10, "Francia", "Europa", 5, 7, "Luis", "AZUL");

        Map<Integer, Country> countries = Map.of(1, new Country());
        Map<Integer, Game> games = Map.of(10, new Game());
        Map<Integer, PlayerGame> players = Map.of(7, new PlayerGame());

        List<CountryGame> entities = CountryGameMapper.toEntityList(List.of(dto), countries, games, players);
        assertEquals(1, entities.size());
        assertEquals(1, entities.get(0).getId().getCountryId());
    }

    @Test
    @DisplayName("toEntityList debería retornar lista vacía si entrada es null o vacía")
    void testToEntityList_EmptyOrNull() {
        Map<Integer, Country> p = new HashMap<>();
        Map<Integer, Game> pa = new HashMap<>();
        Map<Integer, PlayerGame> j = new HashMap<>();
        assertTrue(CountryGameMapper.toEntityList(null, p, pa, j).isEmpty());
        assertTrue(CountryGameMapper.toEntityList(Collections.emptyList(), p, pa, j).isEmpty());
    }
}