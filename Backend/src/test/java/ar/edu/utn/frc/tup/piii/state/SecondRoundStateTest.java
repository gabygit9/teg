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
class SecondRoundStateTest {

    @Mock
    private PlayerService playerService;

    @Mock
    private Game game;

    @Mock
    private GameStateService gameStateService;

    @Mock
    private StateGameEntity stateGameEntity;

    private SecondRoundStateGame secondRoundStateGame;

    @BeforeEach
    void setUp() {
        secondRoundStateGame = new SecondRoundStateGame(playerService, gameStateService);
    }

    @Test
    void testExecuteTurnInStateCorrect() {
        when(game.getStates()).thenReturn(stateGameEntity);
        when(stateGameEntity.getDescription()).thenReturn("SECOND_ROUND");
        when(game.getId()).thenReturn(5);

        HumanPlayer player1 = new HumanPlayer();
        HumanPlayer player2 = new HumanPlayer();

        PlayerGame jp1 = new PlayerGame();
        jp1.setPlayer(player1);

        PlayerGame jp2 = new PlayerGame();
        jp2.setPlayer(player2);

        List<PlayerGame> players = List.of(jp1, jp2);
        when(playerService.findByGameId(5)).thenReturn(players);

        secondRoundStateGame.executeTurn(game);

        assertEquals(3, player1.getAvailableArmies());
        assertEquals(3, player2.getAvailableArmies());

        verify(playerService).update(player1);
        verify(playerService).update(player2);
    }

    @Test
    void testExecutTurnInStateIncorrectThrowException() {
        when(game.getStates()).thenReturn(stateGameEntity);
        when(stateGameEntity.getDescription()).thenReturn("FIRST_ROUND");

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> secondRoundStateGame.executeTurn(game)
        );

        assertEquals("La partida no está en Segunda Ronda.", exception.getMessage());
        verifyNoInteractions(playerService);
    }

}