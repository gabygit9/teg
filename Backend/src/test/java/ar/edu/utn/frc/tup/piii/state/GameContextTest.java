package ar.edu.utn.frc.tup.piii.state;

import ar.edu.utn.frc.tup.piii.model.entities.Game;
import ar.edu.utn.frc.tup.piii.model.entities.StateGameEntity;
import ar.edu.utn.frc.tup.piii.services.interfaces.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameContextTest {

    @Mock
    private GameService gameService;

    @Mock
    private CountryGameService countryGameService;

    @Mock
    private CardService cardService;

    @Mock
    private PlayerService playerService;

    @Mock
    private GameStateService gameStateService;

    @Mock
    private ObjectiveService objectiveService;

    @Mock
    private Game game;

    @Mock
    private StateGameEntity stateGameEntity;

    @Test
    void testConstructorStarCorrectlyPreparation() {
        when(stateGameEntity.getDescription()).thenReturn("PREPARATION");
        when(game.getStates()).thenReturn(stateGameEntity);

        GameContext context = new GameContext(game, gameService, countryGameService, cardService, playerService, gameStateService, objectiveService);

        assertInstanceOf(PreparationStateGame.class, context.getCurrentState());
    }

    @Test
    void testExecuteTurnDelegateToState() {
        StateGame stateMock = mock(StateGame.class);
        GameContext context = new GameContext();
        context = new GameContext();
        context.setCurrentState(stateMock);

        Game game = mock(Game.class);
        context.executeTurn(game);

        verify(stateMock).executeTurn(game);
    }

    @Test
    void testConstructorStartCorrectlySecondRound() {
        when(stateGameEntity.getDescription()).thenReturn("SECOND_ROUND");
        when(game.getStates()).thenReturn(stateGameEntity);

        GameContext context = new GameContext(game, gameService, countryGameService, cardService, playerService, gameStateService, objectiveService);

        assertInstanceOf(SecondRoundStateGame.class, context.getCurrentState());
    }

    @Test
    void testConstructorStartCorrectlyHostilities() {
        when(stateGameEntity.getDescription()).thenReturn("HOSTILITIES");
        when(game.getStates()).thenReturn(stateGameEntity);

        GameContext context = new GameContext(game, gameService, countryGameService, cardService, playerService, gameStateService, objectiveService);

        assertInstanceOf(HostilitiesStateGame.class, context.getCurrentState());
    }

}