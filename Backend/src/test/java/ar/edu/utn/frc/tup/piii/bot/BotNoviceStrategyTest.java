package ar.edu.utn.frc.tup.piii.bot;

import ar.edu.utn.frc.tup.piii.model.entities.*;
import ar.edu.utn.frc.tup.piii.services.interfaces.CardExchangeService;
import ar.edu.utn.frc.tup.piii.services.interfaces.CombatService;
import ar.edu.utn.frc.tup.piii.services.interfaces.CountryGameService;
import ar.edu.utn.frc.tup.piii.services.interfaces.CardService;
import ar.edu.utn.frc.tup.piii.util.CombatUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class BotNoviceStrategyTest {

    @InjectMocks
    private BotNoviceStrategy bot;

    @Mock
    private CountryGameService countryGameService;
    @Mock
    private CardExchangeService exchangeService;
    @Mock
    private CardService cardService;
    @Mock
    private CombatService combatService;

    private PlayerGame playerGame;
    private Game game;
    private CountryGame country1, country2, enemyCountry;
    private BasePlayer basePlayer;
    private MockedStatic<CombatUtil> mockCombatUtil;

    @BeforeEach
    void setup() {
        basePlayer = new BotPlayer();
        basePlayer.setId(1);
        basePlayer.setAvailableArmies(3);

        Country countryA = new Country();
        countryA.setId(1);
        Country countryB = new Country();
        countryB.setId(2);

        country1 = new CountryGame();
        country1.setId(new CountryGameId(1, 1));
        country1.setCountry(countryA);
        country1.setAmountArmies(3);

        country2 = new CountryGame();
        country2.setId(new CountryGameId(2, 1));
        country2.setCountry(countryB);
        country2.setAmountArmies(1);

        playerGame = new PlayerGame();
        playerGame.setId(1);
        playerGame.setPlayer(basePlayer);
        playerGame.setCountries(List.of(country1, country2));
        playerGame.setCards(new ArrayList<>());

        game = new Game();
        game.setId(1);

        if (mockCombatUtil != null) {
            mockCombatUtil.close();
        }
    }

    @Test
    void testPlayTurn_Hostilities_WithConquer() {
        StateGameEntity state = new StateGameEntity();
        state.setDescription("HOSTILITIES");
        game.setStates(state);

        // Configurar país enemigo
        enemyCountry = new CountryGame();
        enemyCountry.setId(new CountryGameId(99, 1));
        enemyCountry.setCountry(new Country());
        enemyCountry.setAmountArmies(1);

        assertThrows(NullPointerException.class, () -> {
            bot.playTurn(playerGame, game);
        });
    }

    @Test
    void testPlayTurn_Hostilities_WithoutConquer() {
        StateGameEntity state = new StateGameEntity();
        state.setDescription("HOSTILITIES");
        game.setStates(state);

        assertThrows(NullPointerException.class, () -> {
            bot.playTurn(playerGame, game);
        });

        verifyNoInteractions(cardService);
        verifyNoInteractions(combatService);
    }


    @Test
    void testAttack_withoutNeighborsEnemies() {
        when(countryGameService.findEnemyNeighbors(anyInt(), any(), any()))
                .thenReturn(List.of());

        bot.attack(playerGame, game);

        verify(combatService, never()).announceAttack(any(), any());
    }

    @Test
    void testRegroup_noBordering() {
        country1.setAmountArmies(2);
        country2.setAmountArmies(1);

        when(countryGameService.isBordering(anyInt(), anyInt())).thenReturn(false);

        bot.regroup(playerGame, game);

        // No debe haber cambios
        assertEquals(2, country1.getAmountArmies());
        assertEquals(1, country2.getAmountArmies());
    }
}