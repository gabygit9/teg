package ar.edu.utn.frc.tup.piii.state;

import ar.edu.utn.frc.tup.piii.model.entities.Game;
import ar.edu.utn.frc.tup.piii.model.entities.HumanPlayer;
import ar.edu.utn.frc.tup.piii.model.entities.PlayerGame;
import ar.edu.utn.frc.tup.piii.model.entities.StateGameEntity;
import ar.edu.utn.frc.tup.piii.model.enums.StateGameEnum;
import ar.edu.utn.frc.tup.piii.services.interfaces.GameStateService;
import ar.edu.utn.frc.tup.piii.services.interfaces.PlayerService;
import ar.edu.utn.frc.tup.piii.services.interfaces.GameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinishedStateTest {

    @Mock
    private PlayerService playerService;

    @Mock
    private GameService gameService;

    @Mock
    private Game game;

    @Mock
    private StateGameEntity stateGameEntity;

    @Mock
    private GameStateService gameStateService;

    private FinishedStateGame finishedStateGame;

    @BeforeEach
    void setUp() {
        finishedStateGame = new FinishedStateGame(playerService, gameService, gameStateService);
    }

    @Test
    void testExecuteTurn_withWinner() {
        when(game.getStates()).thenReturn(stateGameEntity);
        when(stateGameEntity.getDescription()).thenReturn("FINISHED");
        when(game.getId()).thenReturn(123);

        HumanPlayer player = new HumanPlayer();
        player.setName("Carlos");

        PlayerGame winnerPlayer = new PlayerGame();
        winnerPlayer.setPlayer(player);
        winnerPlayer.setObjectiveAchieved(true);

        when(playerService.findByGameId(123)).thenReturn(List.of(winnerPlayer));

        finishedStateGame.executeTurn(game);

        verify(playerService).findByGameId(123);
        verify(gameService).changeState(game, StateGameEnum.FINISHED);
    }

    @Test
    void testExecuteTurn_withoutWinner() {
        when(game.getStates()).thenReturn(stateGameEntity);
        when(stateGameEntity.getDescription()).thenReturn("FINISHED");
        when(game.getId()).thenReturn(456);

        PlayerGame playerGame = new PlayerGame();
        playerGame.setObjectiveAchieved(false);

        when(playerService.findByGameId(456)).thenReturn(List.of(playerGame));

        finishedStateGame.executeTurn(game);

        verify(playerService).findByGameId(456);
        verify(gameService).changeState(game, StateGameEnum.FINISHED);
    }

    @Test
    void testExecuteTurn_stateIncorrect_throwException() {
        when(game.getStates()).thenReturn(stateGameEntity);
        when(stateGameEntity.getDescription()).thenReturn("HOSTILITIES"); // no es FINALIZADA

        assertThrows(IllegalStateException.class, () -> finishedStateGame.executeTurn(game));

        verifyNoInteractions(playerService);
        verifyNoInteractions(gameService);
    }
}