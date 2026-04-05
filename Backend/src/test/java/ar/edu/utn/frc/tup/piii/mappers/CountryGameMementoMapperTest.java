package ar.edu.utn.frc.tup.piii.mappers;

import ar.edu.utn.frc.tup.piii.dto.CountryGameDTO;
import ar.edu.utn.frc.tup.piii.model.entities.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class CountryGameMementoMapperTest {

    @Test
    @DisplayName("toDTO debe mapear correctamente PaisPartida a PaisPartidaMementoDTO")
    void testToDTO() {
        // Arrange
        Continent continent = new Continent();
        continent.setName("Asia");

        Country country = new Country();
        country.setId(1);
        country.setName("China");
        country.setContinent(continent);

        BasePlayer player = new HumanPlayer();
        player.setName("Juan");

        Color color = new Color();
        color.setName("RED");

        PlayerGame playerGame = new PlayerGame();
        playerGame.setId(77);
        playerGame.setPlayer(player);
        playerGame.setColor(color);

        CountryGameId id = new CountryGameId();
        id.setCountryId(1);
        id.setGameId(100);

        CountryGame countryGame = new CountryGame();
        countryGame.setId(id);
        countryGame.setCountry(country);
        countryGame.setPlayerGame(playerGame);
        countryGame.setAmountArmies(5);

        // Act
        CountryGameDTO dto = CountryGameMapper.toDto(countryGame);

        // Assert
        assertEquals(1, dto.getCountryId());
        assertEquals(100, dto.getGameId());
        assertEquals("China", dto.getCountryName());
        assertEquals("Asia", dto.getContinent());
        assertEquals(77, dto.getPlayerId());
        assertEquals("Juan", dto.getPlayerName());
        assertEquals(5, dto.getAvailableArmies());
    }
}