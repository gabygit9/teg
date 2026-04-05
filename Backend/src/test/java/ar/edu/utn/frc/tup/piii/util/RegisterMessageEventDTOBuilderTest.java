package ar.edu.utn.frc.tup.piii.util;

import ar.edu.utn.frc.tup.piii.dto.RegisterMessageEventDTO;
import ar.edu.utn.frc.tup.piii.model.entities.CountryGame;
import ar.edu.utn.frc.tup.piii.model.entities.Game;
import ar.edu.utn.frc.tup.piii.model.entities.CountryGameId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegisterMessageEventDTOBuilderTest {

    @Test
    void testForAttacksAndMovements_CreateDTOCorrectly() {
        Game game = new Game();
        game.setId(1);

        CountryGame origin = new CountryGame();
        origin.setId(new CountryGameId(1, 1));
        origin.setGame(game);

        CountryGame destine = new CountryGame();
        destine.setId(new CountryGameId(2, 1));

        int troops = 5;

        RegisterMessageEventDTO dto = RegisterMessageEventDTOBuilder.forAttacksAndMovements(origin, destine, troops);

        assertEquals(game, dto.getGame());
        assertEquals(origin, dto.getOriginCountry());
        assertEquals(destine, dto.getDestinationCountry());
        assertEquals(5, dto.getAmountTroops());
    }

}