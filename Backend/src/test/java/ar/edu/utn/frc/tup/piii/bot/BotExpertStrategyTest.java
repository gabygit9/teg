package ar.edu.utn.frc.tup.piii.bot;

import ar.edu.utn.frc.tup.piii.dto.BasePlayerDTO;
import ar.edu.utn.frc.tup.piii.mappers.BasePlayerMapper;
import ar.edu.utn.frc.tup.piii.model.entities.*;
import ar.edu.utn.frc.tup.piii.services.interfaces.*;
import ar.edu.utn.frc.tup.piii.util.AnalizeObjective;
import ar.edu.utn.frc.tup.piii.util.CombatUtil;
import ar.edu.utn.frc.tup.piii.util.ProcessedObjective;
import ar.edu.utn.frc.tup.piii.util.ObjectiveType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class BotExpertStrategyTest {

    @Mock
    private CountryGameService countryGameService;
    @Mock
    private CardExchangeService exchangeService;
    @Mock
    private CombatService combatService;
    @Mock
    private PlayerGame playerMock;


    @Spy
    @InjectMocks
    private BotExpertStrategy botStrategy;
    @Mock
    CardService cardService;
    private HistoryService historyService;
    private CardExchangeService cardExchangeService;
    private PlayerGame playerBot;
    private GameService gameService;
    private Game game;
    private List<CountryGame> countriesBot;

    @BeforeEach
    void setUp() {
        // Setup player bot
        BasePlayerDTO player = new BasePlayerDTO();
        player.setId(1);
        player.setAvailableArmies(10);

        playerBot = new PlayerGame();
        playerBot.setId(1);
        playerBot.setPlayer(BasePlayerMapper.toEntity(player));
        playerBot.setActive(true);
        playerBot.setColor(new Color(1, "ROJO"));
        playerBot.setSecretObjective(new Objective(1, "Conquistar 24 territorios"));
        playerBot.setCards(new ArrayList<>());

        // Setup países del bot
        countriesBot = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            CountryGame countryGame = createCountryGame(i, "Pais" + i, playerBot, 5);
            countriesBot.add(countryGame);
        }
        playerBot.setCountries(countriesBot);

        // Setup partida
        StateGameEntity stateGameEntity = new StateGameEntity();
        stateGameEntity.setDescription("HOSTILITIES");

        game = new Game();
        game.setId(1);
        game.setStates(stateGameEntity);
    }

    private CountryGame createCountryGame(int id, String name, PlayerGame playerGame, int armies) {
        Continent continent = new Continent();
        continent.setId(1);
        continent.setName("North America");

        Country country = new Country();
        country.setId(id);
        country.setName(name);
        country.setContinent(continent);

        CountryGame countryGame = new CountryGame();
        countryGame.setId(new CountryGameId(1, id));
        countryGame.setCountry(country);
        countryGame.setPlayerGame(playerGame);
        countryGame.setAmountArmies(armies);

        return countryGame;
    }


    @Test
    void testPlayTurn_SecondRound() {
        StateGameEntity secondState = new StateGameEntity();
        secondState.setDescription("SECOND_ROUND");
        game.setStates(secondState);

        botStrategy.playTurn(playerBot, game);

        verify(countryGameService, times(3)).increaseArmies(any(), eq(1));
    }

    @Test
    void testPlayTurn_Hostilities_WithExchange() {
        PlayerGame playerBot = mock(PlayerGame.class);
        Game game = mock(Game.class);
        StateGameEntity mockState = mock(StateGameEntity.class);
        BasePlayer basePlayerMock = mock(BasePlayer.class);

        when(game.getStates()).thenReturn(mockState);
        when(mockState.getDescription()).thenReturn("HOSTILITIES");

        int playerId = 1;
        int gameId = 99;
        when(playerBot.getId()).thenReturn(playerId);
        when(game.getId()).thenReturn(gameId);

        when(playerBot.getPlayer()).thenReturn(basePlayerMock);
        when(basePlayerMock.getAvailableArmies()).thenReturn(5);

        List<CardPlayer> mockCards = List.of(
                mock(CardPlayer.class),
                mock(CardPlayer.class),
                mock(CardPlayer.class),
                mock(CardPlayer.class),
                mock(CardPlayer.class)
        );
        when(playerBot.getCards()).thenReturn(mockCards);

        CountryGame countryMock = mock(CountryGame.class);
        CountryGame[] countriesBot = new CountryGame[]{countryMock};
        when(countryGameService.findByGame(game)).thenReturn(List.of(countriesBot));
        when(countryGameService.checkVictory(playerBot, game)).thenReturn(false);

        when(exchangeService.canExchange(playerId)).thenReturn(true);

        try (MockedStatic<AnalizeObjective> mockedAnalyzer = mockStatic(AnalizeObjective.class)) {
            ProcessedObjective objectiveMock = mock(ProcessedObjective.class);
            when(objectiveMock.getType()).thenReturn(ObjectiveType.CONTINENT_AND_COUNTRIES);
            mockedAnalyzer.when(() -> AnalizeObjective.analizeObjective(any())).thenReturn(objectiveMock);

            botStrategy.playTurn(playerBot, game);

            verify(exchangeService).doExchange(eq(playerId), eq(gameId));
        }
    }

    @Test
    void testPlayTurn_Hostilities_WithouExchange() {
        Game game = mock(Game.class);
        StateGameEntity state = mock(StateGameEntity.class);
        when(game.getStates()).thenReturn(state);
        when(state.getDescription()).thenReturn("HOSTILITIES");

        PlayerGame playerBot = mock(PlayerGame.class);
        BasePlayer basePlayer = mock(BasePlayer.class);
        when(playerBot.getPlayer()).thenReturn(basePlayer);
        when(basePlayer.getAvailableArmies()).thenReturn(3);
        when(playerBot.getCards()).thenReturn(List.of(new CardPlayer()));
        when(playerBot.getCountries()).thenReturn(List.of());

        List<CountryGame> countries = List.of(mock(CountryGame.class), mock(CountryGame.class));
        when(countryGameService.findByGame(game)).thenReturn(countries);
        when(countryGameService.checkVictory(playerBot, game)).thenReturn(false);

        try (MockedStatic<AnalizeObjective> objectiveMock = mockStatic(AnalizeObjective.class)) {
            ProcessedObjective processedObjective = mock(ProcessedObjective.class);
            when(processedObjective.getType()).thenReturn(ObjectiveType.CONTINENT_AND_COUNTRIES);
            objectiveMock.when(() -> AnalizeObjective.analizeObjective(any())).thenReturn(processedObjective);

            botStrategy.playTurn(playerBot, game);

            verify(exchangeService, never()).doExchange(anyInt(), anyInt());
        }
    }

    @Test
    void testPutInitialArmies_EmptyCountries() {
        playerBot.setCountries(new ArrayList<>());

        StateGameEntity firstState = new StateGameEntity();
        firstState.setDescription("FIRST_ROUND");
        game.setStates(firstState);

        botStrategy.playTurn(playerBot, game);

        verify(countryGameService, never()).increaseArmies(any(), anyInt());
    }

    @Test
    void testDistributeArmies_CorrectAssignation() throws Exception {
        BotExpertStrategy spyBot = Mockito.spy(new BotExpertStrategy());

        CountryGame country1 = mock(CountryGame.class);
        CountryGame country2 = mock(CountryGame.class);
        CountryGame country3 = mock(CountryGame.class);

        Map<CountryGame, Integer> priorities = new LinkedHashMap<>();
        priorities.put(country1, 3);
        priorities.put(country2, 2);
        priorities.put(country3, 1);

        doNothing().when(spyBot).applyAssignation(anyMap());

        Method method = BotExpertStrategy.class.getDeclaredMethod("distributeArmies", int.class, Map.class);
        method.setAccessible(true);

        method.invoke(spyBot, 6, priorities);

        verify(spyBot).applyAssignation(argThat(map -> {
            Integer e1 = map.getOrDefault(country1, 0);
            Integer e2 = map.getOrDefault(country2, 0);
            Integer e3 = map.getOrDefault(country3, 0);
            return e1 == 3 && e2 == 2 && e3 == 1;
        }));
    }

    @Test
    void testDistributeArmiesRemaining_CorrectlyDistribution() throws Exception {
        BotExpertStrategy spyBot = Mockito.spy(new BotExpertStrategy());

        CountryGame country1 = mock(CountryGame.class);
        CountryGame country2 = mock(CountryGame.class);
        CountryGame country3 = mock(CountryGame.class);

        List<CountryGame> ordered = List.of(country1, country2, country3);

        Map<CountryGame, Integer> assignation = new HashMap<>();
        assignation.put(country1, 0);
        assignation.put(country2, 0);
        assignation.put(country3, 0);
        assignation.put(null, 4);

        Method method = BotExpertStrategy.class.getDeclaredMethod(
                "distributeRemainingArmies", int.class, Map.class, List.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        Map<CountryGame, Integer> result = (Map<CountryGame, Integer>) method.invoke(spyBot, 10, assignation, ordered);

        assertEquals(2, result.get(country1));
        assertEquals(1, result.get(country2));
        assertEquals(1, result.get(country3));
    }

    @Test
    void testApplyAssignation_InvoKeServiceWithParametersCorrectly() throws Exception {

        BotExpertStrategy bot = new BotExpertStrategy();
        CountryGameService countryGameService = mock(CountryGameService.class);
        bot.setCountryGameService(countryGameService);

        CountryGame country1 = mock(CountryGame.class);
        CountryGame country2 = mock(CountryGame.class);

        CountryGameId id1 = new CountryGameId(1, 1);
        CountryGameId id2 = new CountryGameId(1, 2);

        when(country1.getId()).thenReturn(id1);
        when(country2.getId()).thenReturn(id2);

        Map<CountryGame, Integer> assignation = new HashMap<>();
        assignation.put(country1, 3);
        assignation.put(country2, 2);

        Method method = BotExpertStrategy.class.getDeclaredMethod("applyAssignation", Map.class);
        method.setAccessible(true);
        method.invoke(bot, assignation);

        verify(countryGameService).increaseArmies(id1, 3);
        verify(countryGameService).increaseArmies(id2, 2);
    }

    @Test
    void testCalculatePriorities_CasesCombinations() throws Exception {

        CountryGame country1 = new CountryGame();
        country1.setCountry(new Country());
        country1.getCountry().setId(1);

        CountryGame country2 = new CountryGame();
        country2.setCountry(new Country());
        country2.getCountry().setId(2);

        CountryGame country3 = new CountryGame();
        country3.setCountry(new Country());
        country3.getCountry().setId(3);

        List<CountryGame> countriesBot = List.of(country1, country2, country3);
        botStrategy.setCountriesBot(countriesBot);

        List<CountryGame> keys = List.of(country1, country3);
        List<CountryGame> threats = List.of(country2, country3);

        Map<CountryGame, Integer> priorities = botStrategy.calculatePriorities(keys, threats);

        assertEquals(2, priorities.get(country1));
        assertEquals(1, priorities.get(country2));
        assertEquals(3, priorities.get(country3));
    }

    @Test
    void testIdentifyKeyTerritories_ContinentAnCountries() {
        ProcessedObjective objective = new ProcessedObjective();
        objective.setType(ObjectiveType.CONTINENT_AND_COUNTRIES);

        Map<String, Integer> countriesPerContinent = new HashMap<>();
        countriesPerContinent.put("Europe", 2);
        objective.setCountriesPerContinent(countriesPerContinent);
        objective.setSingleCountries(List.of("Argentine", "Brazil"));

        CountryGame country1 = mock(CountryGame.class);
        CountryGame country2 = mock(CountryGame.class);
        CountryGame country3 = mock(CountryGame.class);

        BotExpertStrategy spyBot = Mockito.spy(new BotExpertStrategy());

        doReturn(List.of(country1)).when(spyBot).countriesOfContinent(countriesPerContinent);
        doReturn(List.of(country2)).when(spyBot).countriesPerAmount(countriesPerContinent);
        doReturn(List.of(country3)).when(spyBot).remainingCountries(eq(List.of("Argentine", "Brazil")), any());

        List<CountryGame> result = spyBot.identifyKeyTerritories(objective, playerBot, game);

        assertEquals(3, result.size());
        assertTrue(result.containsAll(List.of(country1, country2, country3)));
    }

    @Test
    void testIdentifyKeyTerritories_ArmyColor() {
        ProcessedObjective objective = new ProcessedObjective();
        objective.setType(ObjectiveType.ARMY_COLOR);
        objective.setObjectiveColor("BLUE");

        PlayerGame enemy = mock(PlayerGame.class);
        List<CountryGame> enemyCountries = List.of(mock(CountryGame.class), mock(CountryGame.class));
        when(enemy.getCountries()).thenReturn(enemyCountries);

        BotExpertStrategy spyBot = Mockito.spy(new BotExpertStrategy());
        doReturn(enemy).when(spyBot).searchPlayerByColor("BLUE", game);

        List<CountryGame> result = spyBot.identifyKeyTerritories(objective, playerBot, game);

        assertEquals(enemyCountries, result);
    }

    @Test
    void testIdentifyKeyTerritories_Unknown() {
        ProcessedObjective objective = new ProcessedObjective();
        objective.setType(ObjectiveType.UNKNOWN);

        List<CountryGame> vulnerable = List.of(mock(CountryGame.class), mock(CountryGame.class));

        BotExpertStrategy spyBot = Mockito.spy(new BotExpertStrategy());
        doReturn(vulnerable).when(spyBot).detectVulnerableBorders(playerBot, game);

        List<CountryGame> result = spyBot.identifyKeyTerritories(objective, playerBot, game);

        assertEquals(vulnerable, result);
    }

    @Test
    void testDetectVulnerableBorders_WithBorders() {
        BasePlayer basePlayerBot = new HumanPlayer();
        basePlayerBot.setId(1);
        playerBot.setPlayer(basePlayerBot);

        Country countryBot = new Country();
        countryBot.setId(1);

        CountryGame countryGameBot = new CountryGame();
        countryGameBot.setId(new CountryGameId(1, 1));
        countryGameBot.setCountry(countryBot);
        countryGameBot.setPlayerGame(playerBot);

        BasePlayer basePlayerEnemy = new HumanPlayer();
        basePlayerEnemy.setId(2);

        PlayerGame enemyPlayer = new PlayerGame();
        enemyPlayer.setId(2);
        enemyPlayer.setPlayer(basePlayerEnemy);

        // Simular país enemigo
        Country countryEnemy = new Country();
        countryEnemy.setId(2);

        CountryGame countryGameEnemy = new CountryGame();
        countryGameEnemy.setId(new CountryGameId(1, 2));
        countryGameEnemy.setCountry(countryEnemy);
        countryGameEnemy.setPlayerGame(enemyPlayer);

        botStrategy.setCountriesBot(List.of(countryGameBot));

        when(countryGameService.findByGame(game)).thenReturn(List.of(countryGameBot, countryGameEnemy));
        lenient().when(countryGameService.isBordering(1, 2)).thenReturn(true);

        List<CountryGame> vulnerable = botStrategy.detectVulnerableBorders(playerBot, game);

        assertEquals(1, vulnerable.size());
        assertTrue(vulnerable.contains(countryGameBot));
    }

    @Test
    void testSearchPlayerByColor_ExistPlayerWithColor() {

        PlayerGame redPlayer = new PlayerGame();
        redPlayer.setId(1);
        redPlayer.setActive(true);
        redPlayer.setColor(new Color(1, "RED"));

        Country countryRed = new Country();
        countryRed.setId(1);

        CountryGame countryGameRed = new CountryGame();
        countryGameRed.setId(new CountryGameId(1, 1));
        countryGameRed.setCountry(countryRed);
        countryGameRed.setPlayerGame(redPlayer);

        PlayerGame bluePlayer = new PlayerGame();
        bluePlayer.setId(2);
        bluePlayer.setActive(true);
        bluePlayer.setColor(new Color(2, "BLUE"));

        Country countryBlue = new Country();
        countryBlue.setId(2);

        CountryGame countryGameBlue = new CountryGame();
        countryGameBlue.setId(new CountryGameId(1, 2));
        countryGameBlue.setCountry(countryBlue);
        countryGameBlue.setPlayerGame(bluePlayer);

        when(countryGameService.findByGame(game)).thenReturn(List.of(countryGameRed, countryGameBlue));

        PlayerGame result = botStrategy.searchPlayerByColor("RED", game);

        assertNotNull(result);
        assertEquals(redPlayer.getId(), result.getId());
    }

    @Test
    void testRemainingCountries_CorrectlyFilter() {
        PlayerGame playerGame = new PlayerGame();
        playerGame.setId(1);

        List<String> remainingCountries = List.of("Argentine", "Brazil");

        Country country1 = new Country();
        country1.setName("Argentine");
        CountryGame pp1 = new CountryGame();
        pp1.setCountry(country1);

        Country country2 = new Country();
        country2.setName("Chili");
        CountryGame pp2 = new CountryGame();
        pp2.setCountry(country2);

        Country country3 = new Country();
        country3.setName("Brazil");
        CountryGame pp3 = new CountryGame();
        pp3.setCountry(country3);

        botStrategy.setCountriesBot(List.of(pp1, pp2, pp3));

        Collection<? extends CountryGame> result = botStrategy.remainingCountries(remainingCountries, playerGame);

        assertEquals(2, result.size());
        assertTrue(result.contains(pp1));
        assertTrue(result.contains(pp3));
        assertFalse(result.contains(pp2));
    }

    @Test
    void testCountriesPerAmount_CorrectlyFilter() {
        Continent america = new Continent();
        america.setName("America");
        BasePlayer basePlayer = new HumanPlayer();
        basePlayer.setId(1);

        PlayerGame playerGame = new PlayerGame();
        playerGame.setId(1);
        playerGame.setPlayer(basePlayer);

        CountryGame country1 = new CountryGame();
        country1.setAmountArmies(5);
        country1.setCountry(new Country());
        country1.getCountry().setName("Argentine");
        country1.getCountry().setContinent(america);

        CountryGame country2 = new CountryGame();
        country2.setAmountArmies(3);
        country2.setCountry(new Country());
        country2.getCountry().setName("Brazil");
        country2.getCountry().setContinent(america);

        CountryGame country3 = new CountryGame();
        country3.setAmountArmies(1);
        country3.setCountry(new Country());
        country3.getCountry().setName("Chili");
        country3.getCountry().setContinent(america);

        country1.setPlayerGame(playerGame);
        country2.setPlayerGame(playerGame);
        country3.setPlayerGame(playerGame);


        List<CountryGame> allCountries = List.of(country1, country2, country3);

        botStrategy.setCountriesBot(List.of(country1, country2, country3));

        when(countryGameService.findAll()).thenReturn(allCountries);

        lenient().when(countryGameService.isBordering(anyInt(), anyInt())).thenReturn(false);

        Map<String, Integer> map = new HashMap<>();
        map.put("América", 2);

        Collection<? extends CountryGame> result = botStrategy.countriesPerAmount(map);

        assertEquals(2, result.size());
        assertTrue(result.contains(country1));
        assertTrue(result.contains(country2));
        assertFalse(result.contains(country3));
    }


    @Test
    void testAttack_WithoutMinimumTroops() {
        for (CountryGame country : countriesBot) {
            country.setAmountArmies(1);
        }

        List<CountryGame> allCountries = createAllCountries();
        when(countryGameService.findByGame(game)).thenReturn(allCountries);

        try (MockedStatic<AnalizeObjective> mockedAnalyzer = mockStatic(AnalizeObjective.class)) {
            ProcessedObjective objective = createObjectiveProcessed();
            mockedAnalyzer.when(() -> AnalizeObjective.analizeObjective(any())).thenReturn(objective);

            botStrategy.attack(playerBot, game);

            verify(combatService, never()).announceAttack(any(), any());
        }
    }

    @Test
    void testPutInitialArmies_CountriesNull() {
        playerBot.setCountries(null);

        StateGameEntity firstState = new StateGameEntity();
        firstState.setDescription("FIRST_ROUND");
        game.setStates(firstState);

        botStrategy.playTurn(playerBot, game);

        verify(countryGameService, never()).increaseArmies(any(), anyInt());
    }

    @Test
    void testAmountBorderingEnemies_CorrectlyCounted() {

        BasePlayer onwPlayer = new HumanPlayer();
        onwPlayer.setId(1);

        PlayerGame playerGameOwn = new PlayerGame();
        playerGameOwn.setPlayer(onwPlayer);

        Country countryCentral = new Country();
        countryCentral.setId(100);

        CountryGame country = new CountryGame();
        country.setCountry(countryCentral);
        country.setPlayerGame(playerGameOwn);

        BasePlayer enemy1 = new HumanPlayer();
        enemy1.setId(2);
        PlayerGame enemyGame1 = new PlayerGame();
        enemyGame1.setPlayer(enemy1);

        Country enemyCountry1 = new Country();
        enemyCountry1.setId(200);
        CountryGame enemyCountryGame1 = new CountryGame();
        enemyCountryGame1.setCountry(enemyCountry1);
        enemyCountryGame1.setPlayerGame(enemyGame1);

        BasePlayer enemy2 = new HumanPlayer();
        enemy2.setId(3);
        PlayerGame enemyGame2 = new PlayerGame();
        enemyGame2.setPlayer(enemy2);

        Country enemyCountry2 = new Country();
        enemyCountry2.setId(300);
        CountryGame enemyCountryGame2 = new CountryGame();
        enemyCountryGame2.setCountry(enemyCountry2);
        enemyCountryGame2.setPlayerGame(enemyGame2);

        when(countryGameService.isBordering(100, 200)).thenReturn(true);
        when(countryGameService.isBordering(100, 300)).thenReturn(false);

        List<CountryGame> all = List.of(enemyCountryGame1, enemyCountryGame2);

        int quantity = botStrategy.amountBorderingEnemies(country, all);

        assertEquals(1, quantity);
    }

    @Test
    void testCountriesOfContinent_CorrectlyFilter() {
        Continent america = new Continent(1, "America", 3);
        Continent europe = new Continent(2, "Europe", 3);

        BasePlayer enemy1 = new HumanPlayer();
        enemy1.setId(2);
        PlayerGame enemyGame1 = new PlayerGame();
        enemyGame1.setPlayer(enemy1);

        CountryGame country1 = createCountryGame(1, "Argentine", enemyGame1, 2);
        CountryGame country2 = createCountryGame(2, "Brazil", enemyGame1, 3);
        CountryGame country3 = createCountryGame(3, "France", enemyGame1, 7);
        CountryGame country4 = createCountryGame(4, "Spain", enemyGame1, 8);

        botStrategy.setCountriesBot(List.of(country1, country2, country3, country4));

        Map<String, Integer> request = Map.of(
                "America", 1,
                "Europe", 1
        );

        Collection<? extends CountryGame> result = botStrategy.countriesOfContinent(request);

        assertEquals(0, result.size());
        Set<String> names = result.stream().map(p -> p.getCountry().getName()).collect(Collectors.toSet());
        assertFalse(names.contains("Argentine") || names.contains("Brazil"));
        assertFalse(names.contains("France") || names.contains("Spain"));
    }

    @Test
    void testAttack_WithoutAttackStrategic_ExecuteTraditional() {
        Objective objective = new Objective();
        objective.setDescription("Conquer all");
        playerBot.setSecretObjective(objective);
        when(countryGameService.findByGame(game)).thenReturn(new ArrayList<>());

        try (MockedStatic<AnalizeObjective> mockAnalyzer = mockStatic(AnalizeObjective.class)) {
            ProcessedObjective objectiveMock = mock(ProcessedObjective.class);
            when(objectiveMock.getType()).thenReturn(ObjectiveType.UNKNOWN);
            mockAnalyzer.when(() -> AnalizeObjective.analizeObjective(any())).thenReturn(objectiveMock);

            doReturn(false).when(botStrategy).executeStrategicAttack(any(), anyList(), anyList());
            doNothing().when(botStrategy).executeTraditionalAttack(any(), anyList());

            botStrategy.attack(playerBot, game);

            verify(botStrategy).executeTraditionalAttack(any(), anyList());
        }
    }

    @Test
    void testIsEnemy_PositivesAndNegativesCases() {
        BasePlayer baseEnemy = new HumanPlayer();
        baseEnemy.setId(2);
        PlayerGame enemy = new PlayerGame();
        enemy.setId(2);
        enemy.setPlayer(baseEnemy);

        CountryGame countryWithEnemy = new CountryGame();
        countryWithEnemy.setPlayerGame(enemy);

        PlayerGame me = new PlayerGame();
        me.setId(1);

        assertTrue(botStrategy.isEnemy(countryWithEnemy, me), "Debe detectar enemy (IDs distintos)");

        PlayerGame ally = new PlayerGame();
        ally.setId(1);
        ally.setPlayer(new HumanPlayer());
        ally.getPlayer().setId(1);

        CountryGame allyCountry = new CountryGame();
        allyCountry.setPlayerGame(ally);

        assertFalse(botStrategy.isEnemy(allyCountry, me), "No debe ser enemy si tiene mismo ID");
    }

    @Test
    void testHasTroopsToAttack_LimitCases() {
        CountryGame countryWithOneTroop = new CountryGame();
        countryWithOneTroop.setAmountArmies(1);
        assertFalse(botStrategy.hasTroopsToAttack(countryWithOneTroop), "No debe permitir atacar con 1 tropa");

        CountryGame countryWithTwoTroops = new CountryGame();
        countryWithTwoTroops.setAmountArmies(2);
        assertTrue(botStrategy.hasTroopsToAttack(countryWithTwoTroops), "Debe permitir atacar con 2 tropas");

        CountryGame countryWithThreeTroops = new CountryGame();
        countryWithThreeTroops.setAmountArmies(3);
        assertTrue(botStrategy.hasTroopsToAttack(countryWithThreeTroops), "Debe permitir atacar con más de 2 tropas");
    }

    @Test
    void testGetBorderingEnemies_CorrectlyFilter() {
        Country countryBase = new Country();
        countryBase.setId(1);
        CountryGame attackerCountry = new CountryGame();
        attackerCountry.setCountry(countryBase);

        PlayerGame player = new PlayerGame();
        player.setId(1);
        BasePlayer basePlayer = new HumanPlayer();
        basePlayer.setId(1);
        player.setPlayer(basePlayer);

        Country countryEnemy1 = new Country();
        countryEnemy1.setId(2);
        CountryGame enemy1 = new CountryGame();
        enemy1.setCountry(countryEnemy1);
        PlayerGame enemyPlayer1 = new PlayerGame();
        enemyPlayer1.setId(2);
        enemyPlayer1.setActive(true);
        BasePlayer base1 = new HumanPlayer();
        base1.setId(2);
        enemyPlayer1.setPlayer(base1);
        enemy1.setPlayerGame(enemyPlayer1);

        Country countryNoBorder = new Country();
        countryNoBorder.setId(3);
        CountryGame enemy2 = new CountryGame();
        enemy2.setCountry(countryNoBorder);
        PlayerGame enemyPlayer2 = new PlayerGame();
        enemyPlayer2.setId(3);
        enemyPlayer2.setActive(true);
        BasePlayer base2 = new HumanPlayer();
        base2.setId(3);
        enemyPlayer2.setPlayer(base2);
        enemy2.setPlayerGame(enemyPlayer2);

        Country countryOwn = new Country();
        countryOwn.setId(4);
        CountryGame countryOwnPP = new CountryGame();
        countryOwnPP.setCountry(countryOwn);
        countryOwnPP.setPlayerGame(player);

        List<CountryGame> all = List.of(enemy1, enemy2, countryOwnPP);

        when(countryGameService.isBordering(1, 2)).thenReturn(true);  // enemigo válido
        lenient().when(countryGameService.isBordering(1, 3)).thenReturn(false); // no limitrofe
        lenient().when(countryGameService.isBordering(1, 4)).thenReturn(true);  // propio, no debe incluirse

        List<CountryGame> result = botStrategy.getBorderingEnemies(attackerCountry, all, player);

        assertEquals(1, result.size());
        assertTrue(result.contains(enemy1));
    }

    @Test
    void testExecuteAttackTraditional_WithAttackViable_Conquer() {

        PlayerGame player = mock(PlayerGame.class);
        BasePlayer basePlayer = new HumanPlayer();
        basePlayer.setId(1);

        Country attacker = new Country();
        attacker.setId(1);
        attacker.setName("A");

        Country defensor = new Country();
        defensor.setId(2);
        defensor.setName("B");

        CountryGame attackerPP = new CountryGame();
        attackerPP.setId(new CountryGameId(1, 1));
        attackerPP.setCountry(attacker);
        attackerPP.setAmountArmies(5);
        attackerPP.setPlayerGame(player);

        CountryGame defensorPP = new CountryGame();
        defensorPP.setId(new CountryGameId(1, 2));
        defensorPP.setCountry(defensor);
        defensorPP.setAmountArmies(1);

        PlayerGame enemy = new PlayerGame();
        BasePlayer enemyBase = new HumanPlayer();
        enemyBase.setId(99);
        enemy.setPlayer(enemyBase);
        enemy.setActive(true);
        defensorPP.setPlayerGame(enemy);

        botStrategy.setCountriesBot(List.of(attackerPP));

        List<CountryGame> all = List.of(attackerPP, defensorPP);
        when(countryGameService.getBorder(eq(attackerPP), eq(all)))
                .thenReturn(new CountryGame[]{defensorPP});

        try (MockedStatic<CombatUtil> combatMock = mockStatic(CombatUtil.class)) {
            combatMock.when(() -> CombatUtil.resolveCombat(eq(attackerPP), eq(defensorPP), eq(player), any()))
                    .thenReturn(true);

            botStrategy.executeTraditionalAttack(player, all);

            verify(combatService).announceAttack(eq(attackerPP.getId()), eq(defensorPP.getId()));
            assertTrue(botStrategy.isConqueredThisTurn(), "Debería haber marcado conquista como verdadera");
        }
    }

    @Test
    void testExecuteStrategicAttack_ViablePath() {
        CountryGame origin = mock(CountryGame.class);
        CountryGame destine = mock(CountryGame.class);
        List<CountryGame> all = List.of(origin, destine);
        List<CountryGame> objectives = List.of(destine);
        List<CountryGame> pathMock = List.of(origin, destine);

        BasePlayer basePlayer = new HumanPlayer();
        basePlayer.setId(1);
        PlayerGame playerGame = new PlayerGame();
        playerGame.setPlayer(basePlayer);

        BasePlayer enemyBase = new HumanPlayer();
        enemyBase.setId(2);
        PlayerGame enemyGame = new PlayerGame();
        enemyGame.setPlayer(enemyBase);


        BotExpertStrategy strategy = spy(new BotExpertStrategy());
        strategy.setCountriesBot(List.of(origin));
        doReturn(List.of(origin)).when(strategy).getViableAttackers();

        try (MockedStatic<DijkstraFindPath> dijkstraMock = mockStatic(DijkstraFindPath.class)) {
            dijkstraMock.when(() ->
                    DijkstraFindPath.findShorterPath(eq(origin), eq(destine), eq(all), any())
            ).thenReturn(pathMock);

            doReturn(true).when(strategy).isViableRoute(pathMock, origin, playerGame);

            doNothing().when(strategy).executeAttackPerPath(pathMock, origin, playerGame);

            boolean result = strategy.executeStrategicAttack(playerGame, objectives, all);

            assertTrue(result);
            verify(strategy).executeAttackPerPath(pathMock, origin, playerGame);
        }
    }

    @Test
    void testExecuteStrategicAttack_WithoutViablePath() {

        CountryGame origin = mock(CountryGame.class);
        CountryGame destine = mock(CountryGame.class);
        List<CountryGame> all = List.of(origin, destine);
        List<CountryGame> objectives = List.of(destine);
        List<CountryGame> pathMock = List.of(origin, destine);

        BotExpertStrategy strategy = spy(new BotExpertStrategy());
        strategy.setCountriesBot(List.of(origin));

        doReturn(List.of(origin)).when(strategy).getViableAttackers();

        try (MockedStatic<DijkstraFindPath> dijkstraMock = mockStatic(DijkstraFindPath.class)) {
            dijkstraMock.when(() ->
                    DijkstraFindPath.findShorterPath(eq(origin), eq(destine), eq(all), any())
            ).thenReturn(pathMock);

            doReturn(false).when(strategy).isViableRoute(pathMock, origin, playerMock);

            boolean result = strategy.executeStrategicAttack(playerMock, objectives, all);

            assertFalse(result);
            verify(strategy, never()).executeAttackPerPath(any(), any(), any());
        }
    }


    @Test
    void testExecuteAttackPerPath_Conquer() {

        CountryGame origin = mock(CountryGame.class);
        CountryGame intermediate = mock(CountryGame.class);
        CountryGame destine = mock(CountryGame.class);

        BasePlayer basePlayer = new HumanPlayer();
        basePlayer.setId(10);
        PlayerGame playerGame = new PlayerGame();
        playerGame.setPlayer(basePlayer);

        List<CountryGame> path = List.of(intermediate, destine);

        BotExpertStrategy strategy = spy(new BotExpertStrategy());
        doReturn(true).when(strategy).isEnemy(intermediate, playerGame);
        doReturn(false).when(strategy).isEnemy(destine, playerGame);

        CombatService combatServiceMock = mock(CombatService.class);
        strategy.setCombatService(combatServiceMock);
        strategy.setGameService(mock(GameService.class));

        try (MockedStatic<CombatUtil> combatUtilMock = mockStatic(CombatUtil.class)) {
            combatUtilMock.when(() ->
                            CombatUtil.resolveCombat(origin, intermediate, playerGame, strategy.getGameService()))
                    .thenReturn(true);

            strategy.executeAttackPerPath(path, origin, playerGame);

            verify(combatServiceMock).announceAttack(any(),any());
            combatUtilMock.verify(() ->
                    CombatUtil.resolveCombat(origin, intermediate, playerGame, strategy.getGameService()));

            assertTrue(strategy.isConqueredThisTurn());
        }
    }

    @Test
    void testIsViable_PathViableRoute() {

        CountryGame origin = mock(CountryGame.class);
        CountryGame enemy1 = mock(CountryGame.class);
        CountryGame enemy2 = mock(CountryGame.class);

        BasePlayer basePlayer = new HumanPlayer();
        basePlayer.setId(1);
        PlayerGame playerGame = new PlayerGame();
        playerGame.setPlayer(basePlayer);

        when(origin.getAmountArmies()).thenReturn(20);
        when(enemy1.getAmountArmies()).thenReturn(3);
        when(enemy2.getAmountArmies()).thenReturn(4);

        BotExpertStrategy strategy = spy(new BotExpertStrategy());
        doReturn(true).when(strategy).isEnemy(enemy1, playerGame);
        doReturn(true).when(strategy).isEnemy(enemy2, playerGame);

        List<CountryGame> path = List.of(enemy1, enemy2);

        boolean result = strategy.isViableRoute(path, origin, playerGame);

        assertFalse(result);
    }

    @Test
    void testIsViable_PathNoViableRoute() {
        CountryGame origin = mock(CountryGame.class);
        CountryGame enemy1 = mock(CountryGame.class);

        BasePlayer basePlayer = new HumanPlayer();
        basePlayer.setId(1);
        PlayerGame player = new PlayerGame();
        player.setPlayer(basePlayer);

        BotExpertStrategy strategy = spy(new BotExpertStrategy());

        List<CountryGame> path = List.of(enemy1);

        boolean result = strategy.isViableRoute(path, origin, player);

        assertFalse(result);
    }

    @Test
    void testIsViable_Route_EmptyPath() {
        BotExpertStrategy strategy = new BotExpertStrategy();

        PlayerGame player = new PlayerGame();

        boolean result = strategy.isViableRoute(List.of(), mock(CountryGame.class), player);

        assertFalse(result);
    }

    @Test
    void testGetViableAttackers_CorrectlyFilter() {

        BotExpertStrategy strategy = new BotExpertStrategy();

        CountryGame countriesWithTroops = mock(CountryGame.class);
        when(countriesWithTroops.getAmountArmies()).thenReturn(3);

        CountryGame countriesWithoutTroops = mock(CountryGame.class);
        when(countriesWithoutTroops.getAmountArmies()).thenReturn(1); // TROPAS_MINIMAS_ATAQUE = 2

        strategy.setCountriesBot(List.of(countriesWithTroops, countriesWithoutTroops));

        List<CountryGame> result = strategy.getViableAttackers();

        assertEquals(1, result.size());
        assertTrue(result.contains(countriesWithTroops));
        assertFalse(result.contains(countriesWithoutTroops));
    }

    @Test
    void testGetCountriesObjectives_WithKeyTerritoriesAndEnemies() {

        BotExpertStrategy strategy = spy(new BotExpertStrategy());

        BasePlayer basePlayer = new HumanPlayer();
        basePlayer.setId(1);
        PlayerGame playerGame = new PlayerGame();
        playerGame.setPlayer(basePlayer);

        CountryGame enemy1 = mock(CountryGame.class);
        PlayerGame enemyGame = new PlayerGame();
        BasePlayer baseEnemy = new HumanPlayer();
        baseEnemy.setId(99);
        enemyGame.setPlayer(baseEnemy);
        when(enemy1.getPlayerGame()).thenReturn(enemyGame);

        CountryGame ally = mock(CountryGame.class);
        when(ally.getPlayerGame()).thenReturn(playerGame);

        List<CountryGame> allCountry = List.of(enemy1, ally);

        ProcessedObjective objective = mock(ProcessedObjective.class);

        List<CountryGame> keyTerritoriesMock = List.of(mock(CountryGame.class));
        doReturn(keyTerritoriesMock).when(strategy).identifyKeyTerritories(objective, playerGame, null);

        List<CountryGame> result = strategy.obtenerPaisesObjetivo(playerGame, objective, allCountry);

        assertEquals(2, result.size());
        assertTrue(result.contains(enemy1));
        assertTrue(result.contains(ally));
    }

    @Test
    void testRegroup_ExecuteMovementsViable() {
        BotExpertStrategy strategy = spy(new BotExpertStrategy());

        PlayerGame playerGame = new PlayerGame();
        Game game = new Game();

        CountryGame origin = mock(CountryGame.class);
        CountryGame destine = mock(CountryGame.class);

        List<CountryGame> origins = List.of(origin);
        List<CountryGame> destines = List.of(destine);

        strategy.setCountriesBot(List.of(origin, destine));

        strategy.setCountryGameService(mock(CountryGameService.class));
        strategy.setTurnService(mock(TurnService.class));

        doReturn(origins).when(strategy).getTroopsWithExcessCountries(anyList());
        doReturn(destines).when(strategy).getCountriesInFrontier(eq(playerGame), eq(game));

        try (MockedStatic<DijkstraFindPath> dijkstraMock = mockStatic(DijkstraFindPath.class)) {
            dijkstraMock.when(() ->
                    DijkstraFindPath.findShorterPath(eq(origin), eq(destine), anyList(), any())
            ).thenReturn(List.of(origin, destine));

            strategy.regroup(playerGame, game);

            verify(strategy).executeFirstViableMovement(origins, destines);
        }
    }

    @Test
    void testExecuteFirstMovement() {
        BotExpertStrategy strategy = spy(new BotExpertStrategy());

        CountryGameId originId = new CountryGameId(1, 100);
        CountryGameId destineId = new CountryGameId(2, 100);

        CountryGame origin = mock(CountryGame.class);
        when(origin.getId()).thenReturn(originId);
        when(origin.getAmountArmies()).thenReturn(6);

        CountryGame destine = mock(CountryGame.class);
        when(destine.getId()).thenReturn(destineId);

        strategy.setCountriesBot(List.of(origin, destine));

        TurnService turnService = mock(TurnService.class);
        strategy.setTurnService(turnService);

        List<CountryGame> pathMock = List.of(origin, destine);

        try (MockedStatic<DijkstraFindPath> dijkstraMock = mockStatic(DijkstraFindPath.class)) {
            dijkstraMock.when(() ->
                    DijkstraFindPath.findShorterPath(eq(origin), eq(destine), anyList(), any())
            ).thenReturn(pathMock);

            strategy.executeFirstViableMovement(List.of(origin), List.of(destine));

            verify(turnService).moveArmies(originId, destineId, 3);
        }
    }

    @Test
    void testGetCountriesInFrontier_ReturnCountriesWithEnemyNeighbors() {

        BotExpertStrategy strategy = new BotExpertStrategy();

        Country country = mock(Country.class);
        when(country.getId()).thenReturn(1);

        CountryGame countryBot = mock(CountryGame.class);
        when(countryBot.getCountry()).thenReturn(country);

        strategy.setCountriesBot(List.of(countryBot));

        PlayerGame player = mock(PlayerGame.class);
        Game game = mock(Game.class);

        CountryGameService countryGameService = mock(CountryGameService.class);
        strategy.setCountryGameService(countryGameService);

        CountryGame enemyNeighbor = mock(CountryGame.class);
        when(countryGameService.findEnemyNeighbors(1, player, game)).thenReturn(List.of(enemyNeighbor));

        List<CountryGame> frontier = strategy.getCountriesInFrontier(player, game);

        assertEquals(1, frontier.size());
        assertTrue(frontier.contains(countryBot));
    }

    @Test
    void testGetTroopsWithExcessCountries_CorrectlyFilter() {

        CountryGame country1 = mock(CountryGame.class);
        when(country1.getAmountArmies()).thenReturn(1);

        CountryGame country2 = mock(CountryGame.class);
        when(country2.getAmountArmies()).thenReturn(2);

        CountryGame country3 = mock(CountryGame.class);
        when(country3.getAmountArmies()).thenReturn(5);

        List<CountryGame> allCountries = List.of(country1, country2, country3);

        BotExpertStrategy strategy = new BotExpertStrategy();

        List<CountryGame> result = strategy.getTroopsWithExcessCountries(allCountries);

        assertEquals(2, result.size());
        assertTrue(result.contains(country2));
        assertTrue(result.contains(country3));
        assertFalse(result.contains(country1));
    }


    // Métodos de utilidad para crear objetos de prueba

    private ProcessedObjective createObjectiveProcessed() {
        ProcessedObjective objective = new ProcessedObjective();
        objective.setType(ObjectiveType.UNKNOWN);
        return objective;
    }


    private List<CountryGame> createAllCountries() {
        List<CountryGame> all = new ArrayList<>(countriesBot);
        all.add(createEnemyCountry());
        return all;
    }

    private CountryGame createEnemyCountry() {
        HumanPlayer enemyPlayer = new HumanPlayer();
        enemyPlayer.setId(2);

        PlayerGame enemyPlayerGame = new PlayerGame();
        enemyPlayerGame.setId(2);
        enemyPlayerGame.setPlayer(enemyPlayer);
        enemyPlayerGame.setActive(true);
        enemyPlayerGame.setColor(new ar.edu.utn.frc.tup.piii.model.entities.Color(2, "BLUE"));

        return createCountryGame(99, "CountryEnemy", enemyPlayerGame, 1);
    }



}