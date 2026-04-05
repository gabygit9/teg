package ar.edu.utn.frc.tup.piii.mappers;

import ar.edu.utn.frc.tup.piii.dto.PlayerDTO;
import ar.edu.utn.frc.tup.piii.model.entities.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlayerMapperTest {

    @Test
    void testToDto_Complete() {
        // Arrange
        Objective objective = new Objective();
        objective.setId(1);
        objective.setDescription("Eliminar al playerGame rojo");

        Color color = new Color();
        color.setId(1);
        color.setName("BLUE");

        Country country = new Country();
        country.setId(1);
        country.setName("Argentine");
        Continent cont = new Continent();
        cont.setName("South America");
        country.setContinent(cont);

        BasePlayer basePlayer = new HumanPlayer();
        basePlayer.setName("Juan");

        PlayerGame playerGame = new PlayerGame();
        playerGame.setId(10);
        playerGame.setColor(color);
        playerGame.setObjectiveAchieved(true);
        playerGame.setActive(true);
        playerGame.setTurn(true);
        playerGame.setOrderTurn(2);
        playerGame.setSecretObjective(objective);
        playerGame.setPlayer(basePlayer);

        CountryGame pp = new CountryGame();
        CountryGameId ppId = new CountryGameId();
        ppId.setCountryId(1);
        ppId.setGameId(1);
        pp.setId(ppId);
        pp.setCountry(country);
        pp.setPlayerGame(playerGame);
        pp.setAmountArmies(5);
        playerGame.setCountries(List.of(pp));

        // Act
        PlayerDTO dto = PlayerMapper.toDto(playerGame);

        // Assert
        assertEquals(10, dto.getPlayerGameId());
        assertTrue(dto.isObjectiveAchieved());
        assertEquals(2, dto.getOrderTurn());
        assertEquals(1, dto.getSecretObjectiveId());
        assertEquals("Eliminar al playerGame rojo", dto.getSecretObjectiveDescription());
        assertEquals(1, dto.getCountries().size());
        assertEquals("Argentina", dto.getCountries().get(0).getCountryName());
    }

    @Test
    void testToDto_Null() {
        assertNull(PlayerMapper.toDto(null));
    }

    @Test
    void testToEntity_Complete() {
        // Arrange
        PlayerDTO dto = PlayerDTO.builder()
                .playerGameId(10)
                .orderTurn(1)
                .isTurn(true)
                .objectiveAchieved(true)
                .active(true)
                .build();

        BasePlayer player = new HumanPlayer();
        Objective objective = new Objective();
        List<CountryGame> countries = new ArrayList<>();
        Game game = new Game();
        Color color = new Color();

        // Act
        PlayerGame entity = PlayerMapper.toEntity(dto, player, objective, countries, game, color);

        // Assert
        assertEquals(10, entity.getId());
        assertEquals(1, entity.getOrderTurn());
        assertTrue(entity.isObjectiveAchieved());
        assertEquals(player, entity.getPlayer());
        assertEquals(objective, entity.getSecretObjective());
        assertEquals(game, entity.getGame());
        assertEquals(color, entity.getColor());
    }

    @Test
    void testToEntity_Null() {
        assertNull(PlayerMapper.toEntity(null, null, null, null, null, null));
    }

    @Test
    void testToDtoList_Complete() {
        PlayerGame jp = new PlayerGame();
        jp.setId(1);
        jp.setColor(new Color());
        jp.setActive(true);
        jp.setOrderTurn(1);
        jp.setCountries(new ArrayList<>());
        jp.setSecretObjective(new Objective());

        List<PlayerDTO> result = PlayerMapper.toDtoList(List.of(jp));

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getPlayerGameId());
    }

    @Test
    void testToDtoList_NullOrEmpty() {
        assertTrue(PlayerMapper.toDtoList(null).isEmpty());
        assertTrue(PlayerMapper.toDtoList(new ArrayList<>()).isEmpty());
    }

    @Test
    void testToEntitySimple() {
        PlayerDTO dto = PlayerDTO.builder()
                .playerGameId(33)
                .orderTurn(3)
                .isTurn(true)
                .objectiveAchieved(false)
                .active(true)
                .build();

        PlayerGame jp = PlayerMapper.toEntitySimple(dto);

        assertEquals(33, jp.getId());
        assertEquals(3, jp.getOrderTurn());
        assertTrue(jp.isTurn());
        assertTrue(jp.isActive());
        assertFalse(jp.isObjectiveAchieved());
    }

    @Test
    void testToEntitySimple_Null() {
        assertNull(PlayerMapper.toEntitySimple(null));
    }

}