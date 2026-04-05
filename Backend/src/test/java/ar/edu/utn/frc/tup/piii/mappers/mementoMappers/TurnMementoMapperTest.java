package ar.edu.utn.frc.tup.piii.mappers.mementoMappers;

import ar.edu.utn.frc.tup.piii.dto.mementosDTOs.TurnMementoDTO;
import ar.edu.utn.frc.tup.piii.model.entities.BasePlayer;
import ar.edu.utn.frc.tup.piii.model.entities.PlayerGame;
import ar.edu.utn.frc.tup.piii.model.entities.Turn;
import ar.edu.utn.frc.tup.piii.model.enums.TurnPhase;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TurnMementoMapperTest {

    @Test
    void toDTO() {
        LocalDateTime start = LocalDateTime.of(2025, 7, 1, 12, 0);

        BasePlayer player = new BasePlayer() {};
        player.setName("Ana");

        PlayerGame jp = new PlayerGame();
        jp.setId(10);
        jp.setPlayer(player);

        Turn turn = new Turn();
        turn.setId(3);
        turn.setPlayerGame(jp);
        turn.setCurrentPhase(TurnPhase.ATTACK);
        turn.setInitialStartDate(start);
        turn.setMaxDuration(300);
        turn.setAvailableArmies(5);
        turn.setFinished(true);

        TurnMementoDTO dto = TurnMementoMapper.toDTO(turn);

        assertEquals(3, dto.getId());
        assertEquals(10, dto.getPlayerGameId());
        assertEquals("Ana", dto.getPlayerName());
        assertEquals(start, dto.getDateStartTurn());
        assertEquals(300L, dto.getMaximunDuration());
        assertEquals(5, dto.getAvailableArmies());
        assertTrue(dto.isFinished());
    }
}
