package ar.edu.utn.frc.tup.piii.services.impl;

import ar.edu.utn.frc.tup.piii.dto.CountryGameDTO;
import ar.edu.utn.frc.tup.piii.model.entities.*;
import ar.edu.utn.frc.tup.piii.model.repository.*;
import ar.edu.utn.frc.tup.piii.services.interfaces.*;
import ar.edu.utn.frc.tup.piii.util.RegisterMessageEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CountryGameServiceImplTest {

    @InjectMocks
    private CountryGameServiceImpl service;

    @Mock
    private CountryGameRepository countryGameRepository;

    @Mock
    private CountryConnectionRepository countryConnectionRepository;

    @Mock
    private GameService gameService;

    @Mock
    private PlayerService playerService;

    @Mock
    private CountryRepository countryRepository;

    @Mock
    private RegisterMessageEvent registerMessageEvent;

    @Mock
    private CountryGameService countryGameService;

    @Mock
    private HistoryService historyService;

    private Game game;
    private PlayerGame playerGame;
    private CountryGame countryGame;
    private CountryGameId countryGameId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        game = new Game();
        game.setId(1);

        BasePlayer basePlayer = new HumanPlayer();
        basePlayer.setId(10);
        basePlayer.setAvailableArmies(10);
        basePlayer.setName("Jugador1");

        playerGame = new PlayerGame();
        playerGame.setId(100);
        playerGame.setPlayer(basePlayer);
        playerGame.setGame(game);

        countryGameId = new CountryGameId(1, game.getId());

        countryGame = new CountryGame();
        countryGame.setId(countryGameId);
        countryGame.setPlayerGame(playerGame);
        countryGame.setGame(game);

        Country country = new Country();
        country.setId(1);
        country.setName("Pais1");
        countryGame.setCountry(country);
        countryGame.setAmountArmies(5);
    }

    @Test
    void testSaveNullCountryGame() {
        assertFalse(service.save(null));
        verify(countryGameRepository, never()).save(any());
    }

    @Test
    void testSaveValidCountryGame() {
        when(countryGameRepository.save(countryGame)).thenReturn(countryGame);
        assertTrue(service.save(countryGame));
        verify(countryGameRepository, times(1)).save(countryGame);
    }

    @Test
    void testUpdateNullCountryGame() {
        assertFalse(service.update(null));
        verify(countryGameRepository, never()).existsById(any());
    }

    @Test
    void testUpdateNonExistingCountryGame() {
        when(countryGameRepository.existsById(countryGame.getId())).thenReturn(false);
        assertFalse(service.update(countryGame));
        verify(countryGameRepository, never()).save(any());
    }

    @Test
    void testUpdateExistingCountryGame() {
        when(countryGameRepository.existsById(countryGame.getId())).thenReturn(true);
        when(countryGameRepository.save(countryGame)).thenReturn(countryGame);

        assertTrue(service.update(countryGame));
        verify(countryGameRepository, times(1)).save(countryGame);
    }

    @Test
    void testFindByIdWithEverythingSuccess() {
        when(countryGameRepository.findByIdWithAll(1, 1)).thenReturn(Optional.of(countryGame));
        CountryGame result = service.findById(1, 1);
        assertNotNull(result);
        assertEquals(countryGameId, result.getId());
    }

    @Test
    void testFindByIdWithEverythingFallback() {
        when(countryGameRepository.findByIdWithAll(1, 1)).thenThrow(new RuntimeException("DB error"));
        when(countryGameRepository.findByIdSimple(1, 1)).thenReturn(Optional.of(countryGame));

        CountryGame result = service.findById(1, 1);
        assertNotNull(result);
    }

    @Test
    void testFindByIdNotFound() {
        when(countryGameRepository.findByIdWithAll(1, 1)).thenThrow(new RuntimeException());
        when(countryGameRepository.findByIdSimple(1, 1)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.findById(1, 1));
    }


    @Test
    void testIncreaseArmiesPlayerNotEnough() {
        HumanPlayer humanPlayer = (HumanPlayer) playerGame.getPlayer();
        humanPlayer.setAvailableArmies(3);

        when(countryGameRepository.findById(countryGameId)).thenReturn(Optional.of(countryGame));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            service.increaseArmies(countryGameId, 5);
        });
        assertTrue(ex.getMessage().contains("no tiene suficientes"));
    }

    @Test
    void testIncreaseArmiesPlayerTypeNotSupported() {
        BasePlayer basePlayer = new BasePlayer() { };
        basePlayer.setAvailableArmies(10);
        basePlayer.setId(2);
        basePlayer.setName("JugadorX");
        playerGame.setPlayer(basePlayer);
        countryGame.setPlayerGame(playerGame);

        when(countryGameRepository.findById(countryGameId)).thenReturn(Optional.of(countryGame));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            service.increaseArmies(countryGameId, 5);
        });
        assertTrue(ex.getMessage().contains("no soportado"));
    }

    @Test
    void testReduceArmiesSuccess() {
        when(countryGameRepository.findById(countryGameId)).thenReturn(Optional.of(countryGame));
        countryGame.setAmountArmies(5);

        boolean result = service.reduceArmies(countryGameId, 3);

        assertTrue(result);
        assertEquals(2, countryGame.getAmountArmies());
        verify(countryGameRepository).save(countryGame);
    }

    @Test
    void testReduceArmiesLessThanOneRemaining() {
        when(countryGameRepository.findById(countryGameId)).thenReturn(Optional.of(countryGame));
        countryGame.setAmountArmies(2);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            service.reduceArmies(countryGameId, 2);
        });

        assertTrue(ex.getMessage().contains("aunque sea 1 ejército"));
    }



    @Test
    void testIsBorderingFalse() {
        when(countryConnectionRepository.existsConnection(1, 2)).thenReturn(Optional.empty());

        assertFalse(service.isBordering(1, 2));
    }


    @Test
    void testGetCountriesOfGameNotFounded() {
        when(gameService.findById(1)).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            service.getCountriesOfGame(1);
        });
        assertTrue(ex.getMessage().contains("Partida no encontrada"));
    }

    @Test
    void testDistributeInitialCountriesNormal() {
        when(gameService.findById(1)).thenReturn(game);

        List<Country> countries = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            Country p = new Country();
            p.setId(i);
            countries.add(p);
        }
        when(countryRepository.findAll()).thenReturn(countries);

        PlayerGame player1 = new PlayerGame();
        player1.setId(1);
        player1.setGame(game);

        PlayerGame player2 = new PlayerGame();
        player2.setId(2);
        player2.setGame(game);

        PlayerGame player3 = new PlayerGame();
        player3.setId(3);
        player3.setGame(game);

        when(playerService.findByGameId(1)).thenReturn(List.of(player1, player2, player3));

        when(countryGameRepository.save(any(CountryGame.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<CountryGame> result = service.distributeInitialCountries(1);

        assertNotNull(result);
        assertTrue(result.size() > 0);
        assertTrue(result.stream().allMatch(pp -> pp.getAmountArmies() == 1));
    }

    @Test
    void testDistributeInitialCountriesWithoutPlayers() {
        when(gameService.findById(1)).thenReturn(game);
        when(playerService.findByGameId(1)).thenReturn(Collections.emptyList());

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            service.distributeInitialCountries(1);
        });
        assertTrue(ex.getMessage().contains("No hay jugadores"));
    }

    @Test
    void testCheckVictoryObjectiveAchieved() {
        when(countryGameRepository.findByPlayerGame(playerGame)).thenReturn(new ArrayList<>());
        playerGame.setObjectiveAchieved(true);
        assertTrue(service.checkVictory(playerGame, game));
    }

    @Test
    void testCheckVictoryCountryQuantity() {
        List<CountryGame> countries = new ArrayList<>();
        for (int i = 0; i < 30; i++) countries.add(new CountryGame());
        playerGame.setObjectiveAchieved(false);

        when(countryGameRepository.findByPlayerGame(playerGame)).thenReturn(countries);

        assertTrue(service.checkVictory(playerGame, game));
    }

    @Test
    void testCheckVictoryDifferentGame() {
        Game otraGame = new Game();
        otraGame.setId(2);

        assertFalse(service.checkVictory(playerGame, otraGame));
    }

    @Test
    void test_countryGameToDTO_success() {
        Country country = new Country();
        country.setId(1);
        country.setName("Argentine");

        Continent continent = new Continent();
        continent.setName("South America");
        country.setContinent(continent);

        Game game = new Game();
        game.setId(100);

        BasePlayer player = new HumanPlayer();
        player.setName("Carlos");

        Color color = new Color();
        color.setName("RED");

        PlayerGame playerGame = new PlayerGame();
        playerGame.setId(10);
        playerGame.setPlayer(player);
        playerGame.setColor(color);

        CountryGame pp = new CountryGame();
        pp.setCountry(country);
        pp.setGame(game);
        pp.setAmountArmies(5);
        pp.setPlayerGame(playerGame);

        CountryGameDTO dto = service.countryGameToDTO(pp);

        assertEquals(1, dto.getCountryId());
        assertEquals(100, dto.getGameId());
        assertEquals("Argentine", dto.getCountryName());
        assertEquals("South America", dto.getContinent());
        assertEquals(5, dto.getAvailableArmies());
        assertEquals(10, dto.getPlayerId());
        assertEquals("Carlos", dto.getPlayerName());
        assertEquals("RED", dto.getColor());
    }

    @Test
    void hasNBorderCountriesEachOther_returnTrue_ifThereIsNCountriesConnected() {

        PlayerGame player = new PlayerGame();
        player.setId(1);

        Country country1 = new Country(); country1.setId(1);
        Country country2 = new Country(); country2.setId(2);
        Country country3 = new Country(); country3.setId(3);

        Game game = new Game(); game.setId(100);
        player.setGame(game);

        CountryGame pp1 = new CountryGame(); pp1.setCountry(country1); pp1.setGame(game);
        CountryGame pp2 = new CountryGame(); pp2.setCountry(country2); pp2.setGame(game);
        CountryGame pp3 = new CountryGame(); pp3.setCountry(country3); pp3.setGame(game);

        when(countryGameRepository.findByPlayerGame(player)).thenReturn(List.of(pp1, pp2, pp3));

        when(countryConnectionRepository.existsConnection(1, 2)).thenReturn(Optional.of(new CountryConnection()));
        when(countryConnectionRepository.existsConnection(2, 1)).thenReturn(Optional.of(new CountryConnection()));

        when(countryConnectionRepository.existsConnection(2, 3)).thenReturn(Optional.of(new CountryConnection()));
        when(countryConnectionRepository.existsConnection(3, 2)).thenReturn(Optional.of(new CountryConnection()));

        boolean result = service.hasNBorderCountriesEachOther(player, 3);

        assertTrue(result);
    }


}
