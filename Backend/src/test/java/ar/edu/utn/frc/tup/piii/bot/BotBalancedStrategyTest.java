package ar.edu.utn.frc.tup.piii.bot;

import ar.edu.utn.frc.tup.piii.model.entities.*;
import ar.edu.utn.frc.tup.piii.model.enums.StateGameEnum;
import ar.edu.utn.frc.tup.piii.services.interfaces.*;
import ar.edu.utn.frc.tup.piii.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class BotBalancedStrategyTest {

    @InjectMocks
    private BotBalancedStrategy bot;

    @Mock
    private CountryGameService countryGameService;
    @Mock
    private GameService gameService;
    @Mock
    private CardService cardService;
    @Mock
    private CardExchangeService cardExchangeService;
    @Mock
    private ContinentService continentService;
    @Mock
    private CombatService combatService;

    private PlayerGame playerGame;
    private Game game;

    @BeforeEach
    void setUp() {
        playerGame = createPlayerWithCountries();
        game = createGameWithState("HOSTILITIES");

        // Setear dependencias no inyectadas por @Mock/@InjectMocks
        ReflectionTestUtils.setField(bot, "gameState", StateGameEnum.HOSTILITIES);
    }

    private PlayerGame createPlayerWithCountries() {
        HumanPlayer jb = new HumanPlayer();
        jb.setId(1);
        jb.setAvailableArmies(5);

        PlayerGame j = new PlayerGame();
        j.setId(1);
        j.setPlayer(jb);
        j.setCountries(new ArrayList<>());

        return j;
    }

    private Game createGameWithState(String state) {
        StateGameEntity stateEnt = new StateGameEntity();
        stateEnt.setDescription(state);
        Game p = new Game();
        p.setStates(stateEnt);
        p.setId(1);
        return p;
    }


    @Test
    void testDistributeInPriority() throws Exception {
        List<CountryGame> country = Arrays.asList(
                createCountryGame(1), createCountryGame(3), createCountryGame(2)
        );

        Method method = BotBalancedStrategy.class.getDeclaredMethod("distributeInPriority", List.class, int.class);
        method.setAccessible(true);

        int remaining = (int) method.invoke(bot, country, 2);
        assertEquals(0, remaining);
        assertEquals(2, country.get(0).getAmountArmies());
    }

    private CountryGame createCountryGame(int armies) {
        CountryGame p = new CountryGame();
        p.setAmountArmies(armies);
        return p;
    }

    @Test
    void testPlayTurn_FirstRound() {
        Game game = createGameWithState("FIRST_ROUND");

        BotBalancedStrategy spyBot = Mockito.spy(bot);
        doNothing().when(spyBot).putInitialArmies(playerGame, 5);

        spyBot.playTurn(playerGame, game);

        verify(spyBot).putInitialArmies(playerGame, 5);
        verifyNoInteractions(cardExchangeService, combatService, cardService);
    }

    @Test
    void testPlayTurn_SecondRound() {
        Game game = createGameWithState("SECOND_ROUND");

        BotBalancedStrategy spyBot = Mockito.spy(bot);
        doNothing().when(spyBot).putInitialArmies(playerGame, 3);

        spyBot.playTurn(playerGame, game);

        verify(spyBot).putInitialArmies(playerGame, 3);
        verifyNoInteractions(cardExchangeService, combatService, cardService);
    }

    @Test
    void testPlayTurn_Hostilities_WithoutEschange() {
        playerGame.setCards(new ArrayList<>()); // No tiene tarjetas
        Game game = createGameWithState("HOSTILITIES");

        BotBalancedStrategy spyBot = Mockito.spy(bot);
        doNothing().when(spyBot).distributeArmies(playerGame, game);
        doNothing().when(spyBot).attack(playerGame, game);
        doNothing().when(spyBot).regroup(playerGame, game);

        spyBot.playTurn(playerGame, game);

        verify(spyBot).distributeArmies(playerGame, game);
        verify(spyBot).attack(playerGame, game);
        verify(spyBot).regroup(playerGame, game);
        verify(cardExchangeService, never()).doExchange(anyInt(), anyInt());
    }


    private CountryGame createCountry(int id, int armies) {
        Country country = new Country();
        country.setId(id);
        country.setName("Country" + id);
        Continent continent = new Continent();
        continent.setName("Continent" + id);
        country.setContinent(continent);

        CountryGame pp = new CountryGame();
        pp.setId(new CountryGameId(id, 1));
        pp.setAmountArmies(armies);
        pp.setCountry(country);
        pp.setPlayerGame(playerGame);
        return pp;
    }



    @Test
    void testMethodPrivate_distributeInPriority() throws Exception {
        List<CountryGame> country = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            CountryGame p = createCountry(i + 1, 1);
            p.setAmountArmies(i);
            country.add(p);
        }

        Method method = BotBalancedStrategy.class.getDeclaredMethod("distributeInPriority", List.class, int.class);
        method.setAccessible(true);
        int remaining = (int) method.invoke(bot, country, 3);

        assertEquals(0, remaining);
        assertEquals(1, country.get(0).getAmountArmies());
    }

    @Test
    void testPutInitialArmies_CountriesNull_NoEnter() {
        playerGame.setCountries(null);

        BotBalancedStrategy spyBot = Mockito.spy(bot);
        spyBot.putInitialArmies(playerGame, 3);

        verify(countryGameService, never()).increaseArmies(any(), anyInt());
    }
    @Test
    void testPutInitialArmies_EmptyCountries_NoEnter() {
        playerGame.setCountries(Collections.emptyList());

        BotBalancedStrategy spyBot = Mockito.spy(bot);
        spyBot.putInitialArmies(playerGame, 3);

        verify(countryGameService, never()).increaseArmies(any(), anyInt());
    }

    @Test
    void testDistributeAvailableTroops_DistributeArmiesCorrectly() throws Exception {

        playerGame.getPlayer().setAvailableArmies(6);

        List<CountryGame> fromObjective = List.of(createCountry(1, 0));
        List<CountryGame> borderings = List.of(createCountry(2, 0));
        List<CountryGame> fromBot = List.of(createCountry(3, 0));

        BotBalancedStrategy spyBot = Mockito.spy(bot);

        doReturn(3).when(spyBot).distributeInPriority(eq(fromObjective), eq(6)); // devuelve 3 restantes
        doReturn(1).when(spyBot).distributeInPriority(eq(borderings), eq(3)); // devuelve 1 restante
        doReturn(0).when(spyBot).distributeInPriority(eq(fromBot), eq(1)); // consume el resto

        spyBot.distributeAvailableTroops(borderings, fromObjective, fromBot, playerGame);

        verify(spyBot).distributeInPriority(eq(fromObjective), eq(6));
        verify(spyBot).distributeInPriority(eq(borderings), eq(3));
        verify(spyBot).distributeInPriority(eq(fromBot), eq(1));

        assertEquals(0, playerGame.getPlayer().getAvailableArmies());
    }

    @Test
    void testIsPartOfObjective_ContinentTotal() {
        CountryGame country = createCountry(1, 3);
        country.getCountry().setName("Argentine");
        country.getCountry().getContinent().setName("America");

        PlayerGame enemy = new PlayerGame();
        enemy.setColor(new Color(1, "GREEN"));
        country.setPlayerGame(enemy);

        ProcessedObjective objective = new ProcessedObjective();
        objective.setType(ObjectiveType.CONTINENT_AND_COUNTRIES);
        objective.setTotalContinents(List.of("America"));


        boolean result = bot.isPartOfObjective(country, objective, playerGame);
        assertTrue(result);
    }

    @Test
    void testIsPartOfObjective_CountryPerContinent() {
        CountryGame country = createCountry(1, 3);
        country.getCountry().getContinent().setName("Europe");

        PlayerGame enemy = new PlayerGame();
        enemy.setColor(new Color(1, "GREEN"));
        country.setPlayerGame(enemy);

        ProcessedObjective objective = new ProcessedObjective();
        objective.setType(ObjectiveType.CONTINENT_AND_COUNTRIES);
        Map<String, Integer> map = new HashMap<>();
        map.put("Europe", 3);
        objective.setCountriesPerContinent(map);

        boolean result = bot.isPartOfObjective(country, objective, playerGame);
        assertTrue(result);
    }

    @Test
    void testIsPartOfObjective_CountryRemaining() {
        CountryGame country = createCountry(1, 3);
        country.getCountry().setName("Brazil");

        PlayerGame enemy = new PlayerGame();
        enemy.setColor(new Color(1, "GREEN"));
        country.setPlayerGame(enemy);

        ProcessedObjective objective = new ProcessedObjective();
        objective.setType(ObjectiveType.CONTINENT_AND_COUNTRIES);
        objective.setSingleCountries(List.of("Brazil"));

        boolean result = bot.isPartOfObjective(country, objective, playerGame);
        assertTrue(result);
    }

    @Test
    void testIsPartOfObjectives_BordersEachOther() {
        CountryGame country = createCountry(1, 3);

        PlayerGame enemy = new PlayerGame();
        enemy.setColor(new Color(1, "GREEN"));
        country.setPlayerGame(enemy);

        ProcessedObjective objective = new ProcessedObjective();
        objective.setType(ObjectiveType.CONTINENT_AND_COUNTRIES);
        objective.setSingleCountries(List.of("BORDERING_EACH_OTHER"));

        BotBalancedStrategy spyBot = Mockito.spy(bot);
        doReturn(true).when(spyBot).handleBorderlineObjectives(country, playerGame);

        boolean result = spyBot.isPartOfObjective(country, objective, playerGame);
        assertTrue(result);
    }


    @Test
    void testIsPartOfObjective_ColorArmy() {
        CountryGame country = createCountry(1, 3);
        Color color = new Color(1,"RED");
        country.setPlayerGame(new PlayerGame());
        country.getPlayerGame().setColor(color);

        ProcessedObjective objective = new ProcessedObjective();
        objective.setType(ObjectiveType.ARMY_COLOR);
        objective.setObjectiveColor("RED");

        boolean result = bot.isPartOfObjective(country, objective, playerGame);
        assertTrue(result);
    }

    @Test
    void testIsPartOfObjective_NoOneCondition_False() {
        CountryGame country = createCountry(1, 3);
        country.getCountry().setName("Italy");
        country.getCountry().getContinent().setName("Europe");
        country.setPlayerGame(new PlayerGame());
        country.getPlayerGame().setColor(new Color(1, "GREEN"));

        ProcessedObjective objective = new ProcessedObjective();
        objective.setType(ObjectiveType.ARMY_COLOR);
        objective.setObjectiveColor("BLUE"); // no coincide

        boolean result = bot.isPartOfObjective(country, objective, playerGame);
        assertFalse(result);
    }

    @Test
    void testHandleObjectivesBorderingEachOther_MeetsBoundaryGroups() {
        CountryGame country1 = createCountry(1, 2);
        CountryGame country2 = createCountry(2, 2);
        CountryGame country3 = createCountry(3, 2);
        country1.setCountry(new Country()); country1.getCountry().setId(1);
        country2.setCountry(new Country()); country2.getCountry().setId(2);
        country3.setCountry(new Country()); country3.getCountry().setId(3);

        PlayerGame playerMock = new PlayerGame();
        playerMock.setCountries(List.of(country1, country2, country3));

        CountryGame enemy = createCountry(4, 1);

        BotBalancedStrategy spyBot = Mockito.spy(bot);

        // Todos tienen vecinos entre sí y forman grupo válido
        doReturn(true).when(spyBot).isBorderingAndKnowsAnotherOwnCountry(any(), anyList());
        doReturn(true).when(spyBot).areAllBorderingEachOther(anyList());

        boolean result = spyBot.handleBorderlineObjectives(enemy, playerMock);
        assertFalse(result);
    }

    @Test
    void testCanCompleteBoundaryGroup_GroupValidFormed() {
        CountryGame country1 = createCountry(1, 2);
        CountryGame country2 = createCountry(2, 2);
        country1.setCountry(new Country()); country1.getCountry().setId(1);
        country2.setCountry(new Country()); country2.getCountry().setId(2);

        List<CountryGame> playerCountries = List.of(country1, country2);

        CountryGame enemy = new CountryGame();
        Country country3 = new Country(); country3.setId(3);
        enemy.setCountry(country3);

        List<CountryGame> group = List.of(country1, country2, enemy);

        BotBalancedStrategy spyBot = Mockito.spy(bot);

        doReturn(List.of(group)).when(spyBot).findBorderingGroups(anyList());
        doReturn(true).when(spyBot).areAllBorderingEachOther(group);

        boolean result = spyBot.canCompleteBorderingGroup(enemy, playerCountries);
        assertTrue(result);
    }

    @Test
    void testCanCompleteGroupBoundary_GroupInvalid() {
        CountryGame country1 = createCountry(1, 2);
        CountryGame country2 = createCountry(2, 2);
        country1.setCountry(new Country()); country1.getCountry().setId(1);
        country2.setCountry(new Country()); country2.getCountry().setId(2);

        List<CountryGame> playerCountries = List.of(country1, country2);

        CountryGame enemy = new CountryGame();
        Country country3 = new Country(); country3.setId(3);
        enemy.setCountry(country3);

        List<CountryGame> group = List.of(country1, country2, enemy);

        BotBalancedStrategy spyBot = Mockito.spy(bot);

        doReturn(List.of(group)).when(spyBot).findBorderingGroups(anyList());
        doReturn(false).when(spyBot).areAllBorderingEachOther(group);

        boolean result = spyBot.canCompleteBorderingGroup(enemy, playerCountries);
        assertFalse(result);
    }

    @Test
    void testFindBorderingGroups_ReturnValidGroup() {
        CountryGame country1 = createCountry(1, 2);
        country1.setCountry(new Country()); country1.getCountry().setId(1);
        CountryGame country2 = createCountry(2, 2);
        country2.setCountry(new Country()); country2.getCountry().setId(2);
        CountryGame country3 = createCountry(3, 2);
        country3.setCountry(new Country()); country3.getCountry().setId(3);

        List<CountryGame> countries = List.of(country1, country2, country3);

        BotBalancedStrategy spyBot = Mockito.spy(bot);

        doAnswer(invocation -> {
            CountryGame country = invocation.getArgument(0);
            List<CountryGame> group = invocation.getArgument(3);
            Set<CountryGame> visited = invocation.getArgument(2);
            visited.add(country);
            if (country.getId().getCountryId() == 1) {
                visited.add(country2);
                group.add(country);
                group.add(country2);
            } else if (country.getId().getCountryId() == 3) {
                visited.add(country);
                group.add(country);
            }
            return null;
        }).when(spyBot).findBorderingGroupsDFS(any(), anyList(), anySet(), anyList());

        List<List<CountryGame>> groups = spyBot.findBorderingGroups(countries);

        assertEquals(1, groups.size());
        assertEquals(2, groups.get(0).size());
        assertTrue(groups.get(0).contains(country1));
        assertTrue(groups.get(0).contains(country2));
    }


}