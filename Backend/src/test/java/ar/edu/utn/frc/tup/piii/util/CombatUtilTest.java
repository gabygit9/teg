package ar.edu.utn.frc.tup.piii.util;

import ar.edu.utn.frc.tup.piii.model.entities.*;
import ar.edu.utn.frc.tup.piii.services.interfaces.CombatService;
import ar.edu.utn.frc.tup.piii.services.interfaces.CountryGameService;
import ar.edu.utn.frc.tup.piii.services.interfaces.GameService;
import ar.edu.utn.frc.tup.piii.services.interfaces.TurnService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CombatUtilTest {

    @Mock
    private CombatService combatService;

    @Mock
    private TurnService turnService;

    @Mock
    private CountryGameService countryGameService;

    @Mock
    private GameService gameService;

    private CountryGame attacker;
    private CountryGame defensor;
    private PlayerGame playerGame;

    @BeforeEach
    void setUp() {
        // Asignar los mocks estáticos manualmente
        CombatUtil.combatService = combatService;
        CombatUtil.turnService = turnService;
        CombatUtil.countryGameService = countryGameService;

        // Atacante con 4 ejércitos
        attacker = new CountryGame();
        attacker.setId(new CountryGameId(1, 1));
        attacker.setAmountArmies(4);
        PlayerGame attackerPlayer = new PlayerGame();
        BasePlayer basePlayerAttacker = new HumanPlayer();
        basePlayerAttacker.setId(10);
        attackerPlayer.setPlayer(basePlayerAttacker);
        attacker.setPlayerGame(attackerPlayer);

        // Defensor con 2 ejércitos
        defensor = new CountryGame();
        defensor.setId(new CountryGameId(2, 1));
        defensor.setAmountArmies(2);
        PlayerGame defensorPlayer = new PlayerGame();
        BasePlayer basePlayerDefensor = new HumanPlayer();
        basePlayerDefensor.setId(20);
        defensorPlayer.setPlayer(basePlayerDefensor);
        defensor.setPlayerGame(defensorPlayer);

        // Jugador que realiza el ataque
        playerGame = attackerPlayer;
    }

    @Test
    void resolveCombat_whenAttackWin_shouldConquer() {
        Country countryDefensor = new Country();
        countryDefensor.setId(100);
        defensor.setCountry(countryDefensor);

        lenient().when(combatService.throwDice(eq(20), eq(10), anyInt())).thenReturn(List.of(3, 2));

        boolean result = CombatUtil.resolveCombat(attacker, defensor, playerGame, gameService);

        assertFalse(result);
    }


    @Test
    void resolveCombat_whenThereAreNoEnoughTroops_DoNothing() {
        attacker.setAmountArmies(1); // No puede atacar
        CombatUtil.resolveCombat(attacker, defensor, playerGame, gameService);

        verifyNoInteractions(combatService);
        verifyNoInteractions(turnService);
        verifyNoInteractions(countryGameService);
    }
}