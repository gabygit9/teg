package ar.edu.utn.frc.tup.piii.mappers;

import ar.edu.utn.frc.tup.piii.dto.PlayerCardDto;
import ar.edu.utn.frc.tup.piii.model.entities.Country;
import ar.edu.utn.frc.tup.piii.model.entities.Symbol;
import ar.edu.utn.frc.tup.piii.model.entities.CardPlayer;
import ar.edu.utn.frc.tup.piii.model.entities.CardCountry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CardMapperTest {

    @Test
    @DisplayName("toDto debe mapear correctamente un TarjetaJugador a TarjetaJugadorDto")
    void testToDto() {
        // Arrange
        Country country = new Country();
        country.setName("Argentine");

        Symbol symbol = new Symbol();
        symbol.setType("Ballon");

        CardCountry cardCountry = new CardCountry();
        cardCountry.setCountry(country);
        cardCountry.setSymbol(symbol);

        CardPlayer cardPlayer = new CardPlayer();
        cardPlayer.setId(100);
        cardPlayer.setCardCountry(cardCountry);
        cardPlayer.setUsed(true);

        // Act
        PlayerCardDto dto = CardMapper.toDto(cardPlayer);

        // Assert
        assertEquals(100L, dto.getId());
        assertEquals("Argentine", dto.getCountry());
        assertEquals("Ballon", dto.getSymbol());
        assertTrue(dto.isUsed());
    }
}