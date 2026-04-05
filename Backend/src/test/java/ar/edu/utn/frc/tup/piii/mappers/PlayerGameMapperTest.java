package ar.edu.utn.frc.tup.piii.mappers;

import ar.edu.utn.frc.tup.piii.dto.PlayerGameDto;
import ar.edu.utn.frc.tup.piii.model.entities.*;
import ar.edu.utn.frc.tup.piii.model.enums.TurnPhase;
import ar.edu.utn.frc.tup.piii.services.interfaces.TurnService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(MockitoExtension.class)
class PlayerGameMapperTest {

    @Mock
    private TurnService turnService;

    private PlayerGameMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new PlayerGameMapper(turnService);
    }

    @Test
    @DisplayName("Debe mapear correctamente JugadorPartida a JugadorPartidaDto incluyendo turno")
    void testToDto() {
        // Arrange
        BasePlayer player = new HumanPlayer();
        player.setId(99);
        player.setName("Jugador Test");
        player.setAvailableArmies(10);

        Objective objective = new Objective();
        objective.setId(1);
        objective.setDescription("Conquistar Asia");

        Color color = new Color();
        color.setId(3);
        color.setName("RED");

        PlayerGame jp = new PlayerGame();
        jp.setId(5);
        jp.setColor(color);
        jp.setSecretObjective(objective);
        jp.setPlayer(player);
        jp.setActive(false);
        jp.setTurn(true);

        Turn turn = new Turn();
        turn.setId(99);
        turn.setCurrentPhase(TurnPhase.REGROUPING);

        Mockito.when(turnService.getPlayerGameId(5)).thenReturn(turn);

        // Act
        PlayerGameDto dto = mapper.toDto(jp);

        // Assert
        assertEquals(5, dto.getId());
        assertEquals("RED", dto.getColor());
        assertEquals("Conquistar Asia", dto.getObjective().getDescription());
        assertEquals(99, dto.getPlayer().getId());
        assertTrue(dto.isHuman());
        assertFalse(dto.isDeleted()); // porque jp.isActivo() = false
        assertTrue(dto.isTurn());
        assertEquals(99, dto.getTurnId());
        assertEquals("REGROUP", dto.getCurrentPhase());
    }
}