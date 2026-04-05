package ar.edu.utn.frc.tup.piii.services.impl;

import ar.edu.utn.frc.tup.piii.bot.IBehaviorBot;
import ar.edu.utn.frc.tup.piii.dto.BasePlayerDTO;
import ar.edu.utn.frc.tup.piii.dto.PlayerGameDto;
import ar.edu.utn.frc.tup.piii.model.entities.*;
import ar.edu.utn.frc.tup.piii.model.repository.*;
import ar.edu.utn.frc.tup.piii.services.interfaces.GameService;
import ar.edu.utn.frc.tup.piii.services.interfaces.TurnService;
import ar.edu.utn.frc.tup.piii.services.interfaces.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
class PlayerServiceImplTest {

    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private HumanPlayerRepository humanPlayerRepository;
    @Mock
    private PlayerGameRepository playerGameRepository;
    @Mock
    private ColorRepository colorRepo;
    @Mock
    private LevelBotRepository levelBotRepository;
    @Mock
    private PlayerBotRepository playerBotRepository;
    @Mock
    private TurnService turnService;
    @Mock
    private UserService userService;
    @Mock
    private GameService gameService;
    @Mock
    private IBehaviorBot botNovatoStrategy;

    @InjectMocks
    private PlayerServiceImpl playerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findPlayerById() {
        BasePlayer player = new HumanPlayer();
        player.setId(1);
        player.setName("Pedro");
        player.setAvailableArmies(20);

        when(playerRepository.findById(1)).thenReturn(Optional.of(player));

        Optional<BasePlayerDTO> result = playerService.findById(1);

        assertTrue(result.isPresent());
        assertEquals("Pedro", result.get().getPlayerName());
        assertEquals(20, result.get().getAvailableArmies());
    }

    @Test
    void findAll() {
        BasePlayer j1 = new HumanPlayer();
        j1.setId(1);
        j1.setName("Ana");
        j1.setAvailableArmies(15);
        BasePlayer j2 = new HumanPlayer();
        j2.setId(2);
        j2.setName("Luis");
        j2.setAvailableArmies(10);

        when(playerRepository.findAllPlayers()).thenReturn(List.of(j1, j2));

        List<BasePlayerDTO> players = playerService.findAll();

        assertEquals(2, players.size());
        assertEquals("Ana", players.get(0).getPlayerName());
    }

    @Test
    void saveHumanPlayer() {
        BasePlayerDTO dto = new BasePlayerDTO(1, "Carlos", 25);
        HumanPlayer existent = new HumanPlayer();
        existent.setId(1);
        existent.setAvailableArmies(10);

        when(humanPlayerRepository.findById(1)).thenReturn(Optional.of(existent));
        when(humanPlayerRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        HumanPlayer result = playerService.saveHumanPlayer(dto);

        assertEquals(25, result.getAvailableArmies());
    }

    @Test
    void updatePlayer() {
        BasePlayer player = new HumanPlayer();
        player.setId(3);
        player.setName("Lucía");

        when(playerRepository.save(player)).thenReturn(player);

        boolean result = playerService.update(player);
        assertTrue(result);
    }

    @Test
    void savePlayerGame() {
        int gameId = 1, playerId = 2, colorId = 3;

        Game game = new Game();
        game.setId(gameId);
        BasePlayer player = new HumanPlayer();
        player.setId(playerId);
        Color color = new Color();
        color.setId(colorId);
        color.setName("RED");

        when(gameService.findById(gameId)).thenReturn(game);
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(colorRepo.findById(colorId)).thenReturn(Optional.of(color));
        when(playerGameRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        PlayerGame result = playerService.savePlayerGame(gameId, playerId, colorId);

        assertNotNull(result);
        assertEquals("RED", result.getColor().getName());
    }

    @Test
    void createHumanPlayerAndAssignToGame() {
        BasePlayerDTO dto = new BasePlayerDTO(0, "Juan", 50);
        String userName = "Juan";
        int gameId = 1, colorId = 2;

        User user = new User();
        user.setName("Juan");

        Color color = new Color();
        color.setId(colorId);
        color.setName("GREEN");

        Game game = new Game();
        game.setId(gameId);

        when(playerGameRepository.existsByGame_IdAndPlayer_Id(gameId, 0)).thenReturn(false);
        when(userService.findByName(userName)).thenReturn(user);
        when(humanPlayerRepository.save(any())).thenAnswer(i -> {
            HumanPlayer jh = i.getArgument(0);
            jh.setId(99);
            return jh;
        });
        when(colorRepo.findById(colorId)).thenReturn(Optional.of(color));
        when(gameService.findById(gameId)).thenReturn(game);
        when(playerGameRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        PlayerGame result = playerService.createHumanPlayerAndAssignToGame(dto, userName, gameId, colorId);

        assertNotNull(result);
        assertEquals("GREEN", result.getColor().getName());
    }

    @Test
    void findAll_ReturnPlayersDTO() {
        HumanPlayer j1 = new HumanPlayer();
        j1.setId(1);
        j1.setName("A");
        HumanPlayer j2 = new HumanPlayer();
        j2.setId(2);
        j2.setName("B");

        when(playerRepository.findAllPlayers()).thenReturn(List.of(j1, j2));

        List<BasePlayerDTO> result = playerService.findAll();

        assertEquals(2, result.size());
    }


    @Test
    void findPlayerGameById() {
        PlayerGame playerGame = new PlayerGame();
        playerGame.setId(10);

        when(playerGameRepository.findById(10)).thenReturn(Optional.of(playerGame));

        Optional<PlayerGame> result = playerService.findPlayerGameById(10);

        assertTrue(result.isPresent());
        assertEquals(10, result.get().getId());
    }

    @Test
    void updateArmies() {
        BasePlayer player = new HumanPlayer();
        player.setId(1);
        player.setAvailableArmies(5);

        when(playerRepository.findById(1)).thenReturn(Optional.of(player));
        when(playerRepository.save(any())).thenReturn(player);

        BasePlayerDTO dto = playerService.updateArmies(1, 10);
        assertEquals(10, dto.getAvailableArmies());
    }

    @Test
    void updateArmies_QuantityNegative() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> playerService.updateArmies(1, -5));
        assertEquals("cantidadEjercito no puede ser negativo", exception.getMessage());
    }

    @Test
    void updateArmies_ThrowException() {
        when(playerRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> playerService.updateArmies(99, 5));
    }

    @Test
    void persistConcretePlayer() {
        HumanPlayer human = new HumanPlayer();
        playerService.persistConcretPlayer(human);
        verify(humanPlayerRepository).save(human);
    }

    @Test
    void persistConcretePlayer_ThrowException() {
        BasePlayer otherType = new BasePlayer() {};
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                playerService.persistConcretPlayer(otherType)
        );
        assertTrue(ex.getMessage().contains("Tipo de jugador no soportado"));
    }

    @Test
    void shouldReturnADto_whenAnIdInvalidIsPassed() {
        int userId = 1;
        HumanPlayer player = new HumanPlayer(new User(1, "el pepe", "", "", new Role()));

        BasePlayerDTO ver = new BasePlayerDTO(0, null, 0);

        when(humanPlayerRepository.findByUser_Id(userId)).thenReturn(player);

        BasePlayerDTO res = playerService.findBasePlayerPerUserId(userId);

        assertEquals(ver, res);
    }

    @Test
    void findByGameId() {
        PlayerGame playerGame1 = new PlayerGame();
        playerGame1.setId(1);
        PlayerGame playerGame2 = new PlayerGame();
        playerGame2.setId(2);

        when(playerGameRepository.findByGame_Id(1)).thenReturn(List.of(playerGame1, playerGame2));
        List<PlayerGame> res = playerService.findByGameId(1);

        assertEquals(2, res.size());
        assertEquals(1, res.get(0).getId());
    }

    @Test
    void getPlayerInTurn() {
        PlayerGame playerInTurn = new PlayerGame();
        playerInTurn.setId(42);

        when(turnService.getPlayerInTurn(1)).thenReturn(42);
        when(playerGameRepository.findById(42)).thenReturn(Optional.of(playerInTurn));
        Optional<PlayerGame> result = playerService.getPlayerInTurn(1);

        assertTrue(result.isPresent());
        assertEquals(42, result.get().getId());
    }


    @Test
    void assignHumanInGame() {
        String name = "elpepe";
        int gameId = 1;
        int colorId = 2;

        User user = new User(1, name, "elpepe@gmail.com", "", new Role());
        HumanPlayer newHuman = new HumanPlayer(user);
        newHuman.setName(name);
        newHuman.setId(100);
        newHuman.setAvailableArmies(0);

        Game game = new Game();
        Color color = new Color(colorId, "RED");

        when(userService.findByName(name)).thenReturn(user);
        when(playerRepository.findByName(name)).thenReturn(null);
        when(humanPlayerRepository.save(any())).thenReturn(newHuman);
        when(playerRepository.findById(100)).thenReturn(Optional.of(newHuman));
        when(gameService.findById(gameId)).thenReturn(game);
        when(colorRepo.findById(colorId)).thenReturn(Optional.of(color));
        when(playerGameRepository.save(any())).thenAnswer(i -> {
            PlayerGame playerGame = i.getArgument(0);
            playerGame.setId(99);
            return playerGame;
        });

        PlayerGameDto dto = playerService.assignHumanInGame(gameId, name, colorId);

        assertNotNull(dto);
        assertEquals("elpepe", dto.getPlayer().getPlayerName());
        assertEquals(100, dto.getPlayer().getId());
        assertEquals("RED", dto.getColor());
        assertEquals(0, dto.getPlayer().getAvailableArmies());
    }

    @Test
    void assignHumanInGame_PlayerDoNotExists() {
        String name = "elpepe";
        int gameId = 1;
        int colorId = 2;

        User user = new User(1, name, "elpepe@gmail.com", "123", new Role());
        Game game = new Game();
        Color color = new Color(colorId, "RED");

        HumanPlayer newHuman = new HumanPlayer(user);
        newHuman.setName(name);
        newHuman.setId(100);
        newHuman.setAvailableArmies(0);

        when(userService.findByName(name)).thenReturn(user);
        when(playerRepository.findByName(name)).thenReturn(null);
        when(humanPlayerRepository.save(any())).thenAnswer(i -> {
            HumanPlayer jh = i.getArgument(0);
            jh.setId(100);
            return jh;
        });
        when(playerRepository.findById(100)).thenReturn(Optional.of(newHuman));
        when(gameService.findById(gameId)).thenReturn(game);
        when(colorRepo.findById(colorId)).thenReturn(Optional.of(color));
        when(playerGameRepository.save(any())).thenAnswer(i -> {
            PlayerGame playerGame = i.getArgument(0);
            playerGame.setId(1);
            return playerGame;
        });

        PlayerGameDto result = playerService.assignHumanInGame(gameId, name, colorId);

        assertEquals("RED", result.getColor());
        assertEquals("elpepe", result.getPlayer().getPlayerName());
        assertEquals(100, result.getPlayer().getId());
        assertEquals(0, result.getPlayer().getAvailableArmies());
    }

    @Test
    void getPlayerTurnByGame() {
        PlayerGame playerTurn = new PlayerGame();
        playerTurn.setId(10);

        when(playerGameRepository.findByGame_IdAndIsTurnIsTrue(1)).thenReturn(Optional.of(playerTurn));
        Optional<PlayerGame> result = playerService.getPlayerTurnByGame(1);

        assertTrue(result.isPresent());
        assertEquals(10, result.get().getId());
    }

    @Test
    void getAPlayerInAGame() {
        PlayerGame playerGame = new PlayerGame();
        playerGame.setId(5);

        when(playerGameRepository.findByIdAndGameId(1, 5)).thenReturn(Optional.of(playerGame));
        Optional<PlayerGame> result = playerService.getAPlayerInAGame(1, 5);

        assertTrue(result.isPresent());
        assertEquals(5, result.get().getId());
    }

    @Test
    void save() {
        PlayerGame playerGame = new PlayerGame();
        playerService.save(playerGame);
        verify(playerGameRepository).save(playerGame);
    }

    @Test
    void saveBasePlayer() {
        BasePlayer player = new HumanPlayer();
        playerService.saveBasePlayer(player);
        verify(playerRepository).save(player);
    }

    @Test
    void findAllPlayersGame() {
        PlayerGame jp1 = new PlayerGame();
        PlayerGame jp2 = new PlayerGame();
        when(playerGameRepository.findAll()).thenReturn(List.of(jp1, jp2));

        List<PlayerGame> result = playerService.findAllPlayersGame();

        assertEquals(2, result.size());
    }

    @Test
    void searchAvailableBot() {
        List<BotPlayer> quantityTotalBots = new ArrayList<>();
        //
        for (int i = 1; i <= 15; i++) {
            BotPlayer bot = new BotPlayer();
            bot.setId(i);
            quantityTotalBots.add(bot);
        }

        when(playerBotRepository.findByLevelBot(any())).thenReturn(quantityTotalBots);

        BasePlayer player1 = new HumanPlayer();
        player1.setId(1);
        BasePlayer player2 = new HumanPlayer();
        player2.setId(2);

        PlayerGame jp1 = new PlayerGame();
        jp1.setPlayer(player1);
        PlayerGame jp2 = new PlayerGame();
        jp2.setPlayer(player2);

        List<PlayerGame> playersInGame = new ArrayList<>();
        playersInGame.add(jp1);
        playersInGame.add(jp2);


        when(playerGameRepository.findByGame_Id(100)).thenReturn(playersInGame);

        BotPlayer elected = playerService.searchAvailableBot(100, null);

        assertNotEquals(1, elected.getId());
        assertNotEquals(2, elected.getId());

       //
        boolean onlyNovices = false;
        for (int i = 0; i < 5; i++) {
            if (quantityTotalBots.get(i).getId() == elected.getId()) {
                onlyNovices = true;
                break;
            }
        }
        assertTrue(onlyNovices);
    }

    @Test
    void searchAvailableBot_ThrowException() {
        LevelBot level = new LevelBot(1, "Novato");

        List<PlayerGame> players = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            HumanPlayer player = new HumanPlayer();
            player.setId(200 + i);

            PlayerGame playerGame = new PlayerGame();
            playerGame.setPlayer(player);

            players.add(playerGame);
        }

        when(playerGameRepository.findByGame_Id(1)).thenReturn(players);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            playerService.searchAvailableBot(1, level);
        });
        assertEquals("Partida con cantidad máxima de players.", ex.getMessage());
    }



}