package ar.edu.utn.frc.tup.piii.state;

import ar.edu.utn.frc.tup.piii.model.entities.Game;
import ar.edu.utn.frc.tup.piii.model.entities.StateGameEntity;
import ar.edu.utn.frc.tup.piii.services.interfaces.GameStateService;
import ar.edu.utn.frc.tup.piii.services.interfaces.CountryGameService;
import ar.edu.utn.frc.tup.piii.services.interfaces.GameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class PreparationStateTest {

    @Mock
    private GameService gameService;

    @Mock
    private CountryGameService countryGameService;

    @Mock
    private Game game;

    @Mock
    private StateGameEntity stateGameEntity;

    @Mock
    private GameStateService gameStateService;

    private PreparationStateGame preparationState;

    @BeforeEach
    void setUp() {
        preparationState = new PreparationStateGame(gameService, countryGameService, gameStateService);
    }

    @Test
    void testExecuteTurnInStateCorrect() {
        when(game.getStates()).thenReturn(stateGameEntity);
        when(stateGameEntity.getDescription()).thenReturn("PREPARATION");
        when(game.getId()).thenReturn(1);

        preparationState.executeTurn(game);

        verify(countryGameService).distributeInitialCountries(1);
        verify(gameService).assignSecretObjectives(1);
        verify(gameService).assignCommonObjective(1);
        verify(gameService).assignOrderTurnByDice(game);
    }

    @Test
    void testExecutxTurnInStateIncorrectThrowException() {
        when(game.getStates()).thenReturn(stateGameEntity);
        when(stateGameEntity.getDescription()).thenReturn("HOSTILITIES");

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> preparationState.executeTurn(game)
        );

        assertEquals("La partida no está en estado de preparación.", exception.getMessage());

        verifyNoInteractions(countryGameService);
        verify(gameService, never()).assignSecretObjectives(anyInt());
    }

}