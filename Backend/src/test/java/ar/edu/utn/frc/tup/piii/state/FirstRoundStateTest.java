package ar.edu.utn.frc.tup.piii.state;

import ar.edu.utn.frc.tup.piii.model.entities.HumanPlayer;
import ar.edu.utn.frc.tup.piii.model.entities.PlayerGame;
import ar.edu.utn.frc.tup.piii.model.entities.Game;
import ar.edu.utn.frc.tup.piii.model.entities.StateGameEntity;
import ar.edu.utn.frc.tup.piii.services.interfaces.GameStateService;
import ar.edu.utn.frc.tup.piii.services.interfaces.PlayerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FirstRoundStateTest {

    @Mock
    private PlayerService playerService;

    @Mock
    private Game game;

    @Mock
    private GameStateService gameStateService;

    @Mock
    private StateGameEntity stateGameEntity;

    private FirstRoundStateGame firstRoundStateGame;

    @BeforeEach
    void setUp() {
        firstRoundStateGame = new FirstRoundStateGame(playerService, gameStateService);
    }

    @Test
    void testExecuteTurnInStateCorrect() {
        when(game.getStates()).thenReturn(stateGameEntity);
        when(stateGameEntity.getDescription()).thenReturn("FIRST_ROUND");
        when(game.getId()).thenReturn(10);

        HumanPlayer player1 = new HumanPlayer();
        HumanPlayer player2 = new HumanPlayer();

        PlayerGame jp1 = new PlayerGame();
        jp1.setPlayer(player1);

        PlayerGame jp2 = new PlayerGame();
        jp2.setPlayer(player2);

        List<PlayerGame> players = List.of(jp1, jp2);
        when(playerService.findByGameId(10)).thenReturn(players);

        firstRoundStateGame.executeTurn(game);

        assertEquals(5, player1.getAvailableArmies());
        assertEquals(5, player2.getAvailableArmies());

        verify(playerService).update(player1);
        verify(playerService).update(player2);
    }

    @Test
    void testExecuteTurnInStateIncorrectThrowException() {
        when(game.getStates()).thenReturn(stateGameEntity);
        when(stateGameEntity.getDescription()).thenReturn("HOSTILITIES");

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> firstRoundStateGame.executeTurn(game)
        );

        assertEquals("La partida no está en Primera Ronda.", exception.getMessage());
        verifyNoInteractions(playerService);
    }

}