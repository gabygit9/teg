package ar.edu.utn.frc.tup.piii.state;

import ar.edu.utn.frc.tup.piii.model.entities.*;
import ar.edu.utn.frc.tup.piii.services.interfaces.GameStateService;
import ar.edu.utn.frc.tup.piii.services.interfaces.PlayerService;
import ar.edu.utn.frc.tup.piii.services.interfaces.ObjectiveService;
import ar.edu.utn.frc.tup.piii.services.interfaces.GameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HostilitiesStateTest {

    @Mock
    private PlayerService playerService;

    @Mock
    private Game game;

    @Mock
    private ObjectiveService objectiveService;

    @Mock
    private GameService gameService;

    @Mock
    private GameStateService gameStateService;

    @Mock
    private StateGameEntity stateGameEntity;

    private HostilitiesStateGame hostilitiesState;

    @BeforeEach
    void setUp() {
        hostilitiesState = new HostilitiesStateGame(playerService, objectiveService, gameService, gameStateService);
    }

    @Test
    void testExecuteTurnThrowExceptionIfStateIncorrect() {
        when(game.getStates()).thenReturn(stateGameEntity);
        when(stateGameEntity.getDescription()).thenReturn("SECOND_ROUND");

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> hostilitiesState.executeTurn(game));

        assertEquals("La partida no está en fase de hostilidades.", ex.getMessage());
    }

}