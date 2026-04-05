package ar.edu.utn.frc.tup.piii.services.impl;

import ar.edu.utn.frc.tup.piii.dto.BigJsonDTO;
import ar.edu.utn.frc.tup.piii.dto.PlayerGameDto;
import ar.edu.utn.frc.tup.piii.dto.CountryGameDTO;
import ar.edu.utn.frc.tup.piii.dto.GameDTO;
import ar.edu.utn.frc.tup.piii.model.entities.*;
import ar.edu.utn.frc.tup.piii.model.repository.PlayerGameRepository;
import ar.edu.utn.frc.tup.piii.model.repository.GameRepository;
import ar.edu.utn.frc.tup.piii.services.interfaces.*;
import ar.edu.utn.frc.tup.piii.state.GameContext;
import ar.edu.utn.frc.tup.piii.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.within;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class GameServiceImplTest {

    @Mock
    private GameRepository gameRepository;

    @InjectMocks
    private GameServiceImpl gameService;

    @Mock
    private TurnService turnService;

    @Mock
    private HistoryService historyService;

    @Mock
    private RegisterMessageEvent registerMessageEvent;

    @Mock
    private PlayerService playerService;

    @Mock
    private GameStateService gameStateService;

    @Mock
    private ObjectiveService objectiveService;

    @Mock
    private CountryGameService countryGameService;

    @Mock
    private PlayerGameRepository playerGameRepository;

    @BeforeEach
    void setUp() {
        CombatUtil.countryGameService = mock(CountryGameService.class);
    }


    @Test
    void assignCommonObjective_success() {
        int gameId = 1;
        Game game = createGame(gameId);
        game.setObjectiveCommon(null);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(gameRepository.save(any(Game.class))).thenReturn(game);

        gameService.assignCommonObjective(gameId);

        assertThat(game.getObjectiveCommon()).isNull();

        verify(gameRepository, times(1)).findById(gameId);
        verify(gameRepository, times(1)).save(game);
    }

    @Test
    void playersOfAGame_success() {
        int gameId = 1;
        List<PlayerGame> expectedPlayers = createPlayers(3);

        when(playerService.findByGameId(gameId)).thenReturn(expectedPlayers);

        List<PlayerGame> result = playerService.findByGameId(gameId);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result).isEqualTo(expectedPlayers);

        verify(playerService, times(1)).findByGameId(gameId);
    }

    @Test
    void assignSecretObjectives_success() {
        int gameId = 1;
        Game game = createGame(gameId);
        List<PlayerGame> players = createPlayers(4);
        List<Objective> objective = createObjectives();

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(playerService.findByGameId(gameId)).thenReturn(players);
        when(objectiveService.findAll()).thenReturn(new ArrayList<>(objective));

        assertThrows(IllegalStateException.class, () -> {
            gameService.assignSecretObjectives(gameId);
        });

        verify(playerGameRepository, times(0)).save(any(PlayerGame.class));

        for(PlayerGame playerGame : players) {
            assertNull(playerGame.getSecretObjective());
        }
    }
    @Test
    void assignSecretObjectives_doesntAssignObjectiveOfTheSameColor() {

        int gameId = 1;
        Game game = createGame(gameId);

        PlayerGame redPlayer = createPlayer(1, createColor("RED"));
        PlayerGame bluePlayer = createPlayer(2, createColor("BLUE"));
        PlayerGame greenPlayer = createPlayer(3, createColor("GREEN"));
        List<PlayerGame> players = Arrays.asList(redPlayer, bluePlayer, greenPlayer);

        Objective objectiveRemoveRed = createObjectiveSpecific(100, "Eliminar todos los ejércitos rojo");
        Objective objectiveRemoveBlue = createObjectiveSpecific(101, "Eliminar todos los ejércitos azul");
        Objective objectiveCountries = createObjectiveSpecific(103, "Conquistar 24 países");


        List<Objective> objectives = new ArrayList<>();
        objectives.add(objectiveRemoveRed);
        objectives.add(objectiveRemoveBlue);
        objectives.add(objectiveCountries);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(playerService.findByGameId(gameId)).thenReturn(players);
        when(objectiveService.findAll()).thenReturn(new ArrayList<>(objectives));

        // Mock del AnalizadorObjetivo
        try (MockedStatic<AnalizeObjective> mockedStatic = mockStatic(AnalizeObjective.class)) {

            ProcessedObjective objectiveRed = new ProcessedObjective();
            objectiveRed.setType(ObjectiveType.ARMY_COLOR);
            objectiveRed.setObjectiveColor("RED");

            ProcessedObjective objectiveBlue = new ProcessedObjective();
            objectiveBlue.setType(ObjectiveType.ARMY_COLOR);
            objectiveBlue.setObjectiveColor("BLUE");


            ProcessedObjective objectiveCountryCont = new ProcessedObjective();
            objectiveCountryCont.setType(ObjectiveType.CONTINENT_AND_COUNTRIES);

            mockedStatic.when(() -> AnalizeObjective.analizeObjective(any(Objective.class)))
                    .thenAnswer(invocation -> {
                        Objective objective = invocation.getArgument(0);
                        if (objective.getId() == 100) return objectiveRed;
                        if (objective.getId() == 101) return objectiveBlue;
                        if (objective.getId() == 103) return objectiveCountryCont;
                        return objectiveCountryCont; // default
                    });

            assertThrows(IllegalStateException.class, () -> {
                gameService.assignSecretObjectives(gameId);
            });

            verify(playerGameRepository, times(0)).save(any(PlayerGame.class));

            assertThat(redPlayer.getSecretObjective()).isNotSameAs(objectiveRemoveRed);
            assertThat(redPlayer.getSecretObjective()).isNull();


            assertThat(greenPlayer.getSecretObjective()).isNull();
            assertThat(bluePlayer.getSecretObjective()).isNull();
        }
    }

    @Test
    void assignObjectivesSecrets_withoutObjectivesAvailable() {
        // Given
        int gameId = 1;
        Game game = createGame(gameId);
        List<PlayerGame> players = createPlayers(3);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(playerService.findByGameId(gameId)).thenReturn(players);
        when(objectiveService.findAll()).thenReturn(Collections.emptyList());

        // When & Then
        assertThatThrownBy(() -> gameService.assignSecretObjectives(gameId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No se pudo asignar un objetivo válido al jugador");
    }

    @Test
    void assignSecretObjectives_withoutPlayers() {
        // Given
        int gameId = 1;
        Game game = createGame(gameId);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(playerService.findByGameId(gameId)).thenReturn(Collections.emptyList());

        // When & Then
        assertThatThrownBy(() -> gameService.assignSecretObjectives(gameId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No hay jugadores en la partida");
    }

    @Test
    void assignSecretObjectives_lessThan3Players() {
        // Given
        int gameId = 1;
        Game game = createGame(gameId);
        List<PlayerGame> players = createPlayers(2);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(playerService.findByGameId(gameId)).thenReturn(players);

        // When & Then
        assertThatThrownBy(() -> gameService.assignSecretObjectives(gameId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("La cantidad de players debe estar entre 3 y 6");
    }

    @Test
    void assignSecretObjectives_moreThan6Players() {
        // Given
        int gameId = 1;
        Game game = createGame(gameId);
        List<PlayerGame> players = createPlayers(7);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(playerService.findByGameId(gameId)).thenReturn(players);

        // When & Then
        assertThatThrownBy(() -> gameService.assignSecretObjectives(gameId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("La cantidad de players debe estar entre 3 y 6");
    }

    @Test
    void assignOrderTurnByDice_success() {
        Game game = createGame(1);
        List<PlayerGame> players = createPlayers(4);

        when(playerService.findByGameId(game.getId())).thenReturn(players);

        // Mock Random para controlar las tiradas
        try (MockedConstruction<Random> mockedConstruction = mockConstruction(Random.class,
                (mock, context) -> {
                    // Configurar el comportamiento del Random mockeado
                    when(mock.nextInt(6)).thenReturn(5, 3, 4, 2); // +1 = 6,4,5,3
                })) {

            gameService.assignOrderTurnByDice(game);

            verify(playerGameRepository, times(4)).save(any(PlayerGame.class));

            // Verificar que se creó una instancia de Random
            assertEquals(1, mockedConstruction.constructed().size());

            // Verificar orden: jugador1(6) -> orden 1, jugador3(5) -> orden 2,
            // jugador2(4) -> orden 3, jugador4(3) -> orden 4
            assertThat(players.get(0).getOrderTurn()).isEqualTo(1);
            assertThat(players.get(0).isTurn()).isTrue();

            assertThat(players.get(2).getOrderTurn()).isEqualTo(2);
            assertThat(players.get(2).isTurn()).isFalse();

            assertThat(players.get(1).getOrderTurn()).isEqualTo(3);
            assertThat(players.get(1).isTurn()).isFalse();

            assertThat(players.get(3).getOrderTurn()).isEqualTo(4);
            assertThat(players.get(3).isTurn()).isFalse();
        }
    }

    @Test
    void assignOrderTurnByDice_gameWithoutPlayers() {
        Game game = createGame(1);
        when(playerService.findByGameId(game.getId())).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> gameService.assignOrderTurnByDice(game))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No hay jugadores en la partida");
    }

    @Test
    void assignOrderTurnByDice_handleTied() {
        Game game = createGame(1);
        List<PlayerGame> players = createPlayers(2);

        when(playerService.findByGameId(game.getId())).thenReturn(players);

        try (MockedConstruction<Random> mockedConstruction = mockConstruction(Random.class,
                (mock, context) -> {
                    // Jugador 1: 3 (4)
                    // Jugador 2: 3 (empate), 3 (empate), 2 (3)
                    when(mock.nextInt(6)).thenReturn(3, 3, 3, 2);
                })) {

            gameService.assignOrderTurnByDice(game);

            Random randomMock = mockedConstruction.constructed().get(0);

            // Verificar que se llamó 4 veces: 1 + 3 (empates hasta encontrar valor único)
            verify(randomMock, times(4)).nextInt(6);

            // Verificar orden: jugador1(4) -> orden 1, jugador2(3) -> orden 2
            assertThat(players.get(0).getOrderTurn()).isEqualTo(1);
            assertThat(players.get(1).getOrderTurn()).isEqualTo(2);
        }
    }

    @Test
    void assignOrderTurnByDice_playersNull() {
        Game game = createGame(1);
        when(playerService.findByGameId(game.getId())).thenReturn(null);

        assertThatThrownBy(() -> gameService.assignOrderTurnByDice(game))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No hay jugadores en la partida");
    }

    @Test
    void assignInitialArmies_3players_success() {
        int gameId = 1;
        Game game = createGame(gameId);
        List<PlayerGame> players = createPlayersWithCountries(3, Arrays.asList(5, 6, 4)); // 5, 6, 4 países respectivamente

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(playerService.findByGameId(gameId)).thenReturn(players);

        gameService.assignInitialArmies(gameId);

        assertThat(players.get(0).getPlayer().getAvailableArmies()).isEqualTo(30);
        assertThat(players.get(1).getPlayer().getAvailableArmies()).isEqualTo(29);
        assertThat(players.get(2).getPlayer().getAvailableArmies()).isEqualTo(31);

        // Verificar que se actualizó cada jugador
        verify(playerService, times(3)).update(any(HumanPlayer.class));
        verify(playerService).update(players.get(0).getPlayer());
        verify(playerService).update(players.get(1).getPlayer());
        verify(playerService).update(players.get(2).getPlayer());
    }


    @Test
    void assignInitialArmies_4players_success() {
        int gameId = 1;
        Game game = createGame(gameId);
        List<PlayerGame> players = createPlayersWithCountries(4, Arrays.asList(4, 5, 3, 6));

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(playerService.findByGameId(gameId)).thenReturn(players);

        gameService.assignInitialArmies(gameId);

        // Para 4 jugadores: 30 ejércitos iniciales
        assertThat(players.get(0).getPlayer().getAvailableArmies()).isEqualTo(26); // 30 - 4
        assertThat(players.get(1).getPlayer().getAvailableArmies()).isEqualTo(25); // 30 - 5
        assertThat(players.get(2).getPlayer().getAvailableArmies()).isEqualTo(27); // 30 - 3
        assertThat(players.get(3).getPlayer().getAvailableArmies()).isEqualTo(24); // 30 - 6

        verify(playerService, times(4)).update(any(HumanPlayer.class));
    }

    @Test
    void assignInitialArmies_5players_success() {
        int gameId = 1;
        Game game = createGame(gameId);
        List<PlayerGame> players = createPlayersWithCountries(5, Arrays.asList(3, 4, 2, 5, 4));

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(playerService.findByGameId(gameId)).thenReturn(players);

        gameService.assignInitialArmies(gameId);

        // Para 5 players: 25 ejércitos iniciales
        assertThat(players.get(0).getPlayer().getAvailableArmies()).isEqualTo(22); // 25 - 3
        assertThat(players.get(1).getPlayer().getAvailableArmies()).isEqualTo(21); // 25 - 4
        assertThat(players.get(2).getPlayer().getAvailableArmies()).isEqualTo(23); // 25 - 2
        assertThat(players.get(3).getPlayer().getAvailableArmies()).isEqualTo(20); // 25 - 5
        assertThat(players.get(4).getPlayer().getAvailableArmies()).isEqualTo(21); // 25 - 4

        verify(playerService, times(5)).update(any(HumanPlayer.class));
    }

    @Test
    void assignInitialArmies_6players_success() {
        int gameId = 1;
        Game game = createGame(gameId);
        List<PlayerGame> players = createPlayersWithCountries(6, Arrays.asList(2, 3, 4, 2, 3, 2));

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(playerService.findByGameId(gameId)).thenReturn(players);

        gameService.assignInitialArmies(gameId);

        // Para 6 players: 20 ejércitos iniciales
        assertThat(players.get(0).getPlayer().getAvailableArmies()).isEqualTo(18); // 20 - 2
        assertThat(players.get(1).getPlayer().getAvailableArmies()).isEqualTo(17); // 20 - 3
        assertThat(players.get(2).getPlayer().getAvailableArmies()).isEqualTo(16); // 20 - 4
        assertThat(players.get(3).getPlayer().getAvailableArmies()).isEqualTo(18); // 20 - 2
        assertThat(players.get(4).getPlayer().getAvailableArmies()).isEqualTo(17); // 20 - 3
        assertThat(players.get(5).getPlayer().getAvailableArmies()).isEqualTo(18); // 20 - 2

        verify(playerService, times(6)).update(any(HumanPlayer.class));
    }

    @Test
    void assignInitialArmies_7players_throwException() {
        int gameId = 1;
        Game game = createGame(gameId);
        List<PlayerGame> players = createPlayersWithCountries(7, Arrays.asList(1, 2, 3, 4, 5, 6, 7));

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(playerService.findByGameId(gameId)).thenReturn(players);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> gameService.assignInitialArmies(gameId));

        assertThat(exception.getMessage()).isEqualTo("Número inválido de players para asignar ejércitos");
        verify(playerService, never()).update(any(HumanPlayer.class));
    }

    

    //HELPERS

    private Game createGame(int id) {
        Game game = new Game();
        game.setId(id);
        return game;
    }
    private List<PlayerGame> createPlayers(int quantity) {
        List<PlayerGame> players = new ArrayList<>();
        String[] colors = {"RED", "BLUE", "GREEN", "YELLOW","BLACK","WHITE"};

        for (int i = 0; i < quantity; i++) {
            PlayerGame player = createPlayer(i + 1, createColor(colors[i % colors.length]));
            players.add(player);
        }
        return players;
    }

    private PlayerGame createPlayer(int id, Color color) {
        PlayerGame playerGame = new PlayerGame();
        playerGame.setId(id);
        playerGame.setColor(color);
        return playerGame;
    }

    private Color createColor(String name) {
        Color color = new Color();
        color.setName(name);
        return color;
    }

    private List<Objective> createObjectives() {
        return Arrays.asList(
                createObjectiveRemoveColor("RED"),
                createObjectiveRemoveColor("BLUE"),
                createObjectiveConquerContinents(),
                createObjectiveConquerCountries()
        );
    }

    private Objective createObjectiveRemoveColor(String color) {
        Objective objective = new Objective();
        objective.setId(1);
        objective.setDescription("Eliminar todos los ejércitos " + color.toLowerCase());
        return objective;
    }

    private Objective createObjectiveConquerContinents() {
        Objective objective = new Objective();
        objective.setId(2);
        objective.setDescription("Conquistar América del Norte y África");
        return objective;
    }

    private Objective createObjectiveConquerCountries() {
        Objective objective = new Objective();
        objective.setId(3);
        objective.setDescription("Conquistar 24 países");
        return objective;
    }

    private Objective createObjectiveSpecific(int id, String description) {
        Objective objective = new Objective();
        objective.setId(id);
        objective.setDescription(description);
        return objective;
    }

    private List<PlayerGame> createPlayersWithCountries(int quantity, List<Integer> quantityCountry) {
        List<PlayerGame> players = new ArrayList<>();

        for (int i = 0; i < quantity; i++) {
            HumanPlayer player = new HumanPlayer();
            player.setId(i + 1);
            player.setName("Jugador" + (i + 1));

            PlayerGame playerGame = new PlayerGame();
            playerGame.setPlayer(player);

            // Crear países para el player
            List<CountryGame> countries = new ArrayList<>();
            for (int j = 0; j < quantityCountry.get(i); j++) {
                Country country = new Country();
                country.setId(i * 10 + j + 1); // IDs únicos
                country.setName("Pais" + (i * 10 + j + 1));
                Game game = new Game();
                game.setId(1);
                CountryGameId countryGameId = new CountryGameId(country.getId(), game.getId());
                CountryGame countryGame = new CountryGame();
                countryGame.setId(countryGameId);
                countryGame.setCountry(country);
                countryGame.setGame(game);
                countryGame.setPlayerGame(playerGame);
                countries.add(countryGame);
            }
            playerGame.setCountries(countries);

            players.add(playerGame);
        }

        return players;
    }

    @Test
    void assignCommonObjective_gameNotFound_throwException() {
        int gameId = 99;
        when(gameRepository.findById(gameId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameService.assignCommonObjective(gameId))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Partida");
    }

    @Test
    void playersOfAGame_delegatesAndReturnsList() {
        int gameId = 2;
        List<PlayerGame> players = createPlayers(4);
        when(playerService.findByGameId(gameId)).thenReturn(players);

        List<PlayerGame> result = gameService.getPlayerGame(gameId);

        assertThat(result).isEqualTo(players);
        verify(playerService, times(1)).findByGameId(gameId);
    }

    @Test
    void dtoToEntity_success() {
        // Given
        GameDTO dto = new GameDTO();
        dto.setDateTime(LocalDateTime.now());
        dto.setStateId(1);
        dto.setCommunicationType(new CommunicationType(1, "CHAT"));
        dto.setCommonObjectiveId(2);

        StateGameEntity state = new StateGameEntity();
        state.setId(1);
        state.setDescription("INICIADA");

        Objective objective = new Objective();
        objective.setId(2);
        objective.setDescription("Conquistar el mundo");

        when(gameStateService.findById(1)).thenReturn(state);
        when(objectiveService.findById(2)).thenReturn(objective);

        Game result = gameService.dtoToEntity(dto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(0);
        assertThat(result.getStartDate()).isEqualTo(dto.getDateTime());
        assertThat(result.getStates()).isEqualTo(state);
        assertThat(result.getCommunicationType()).isEqualTo(new CommunicationType(1, "CHAT"));
        assertThat(result.getObjectiveCommon()).isEqualTo(objective);

        verify(gameStateService, times(1)).findById(1);
        verify(objectiveService, times(1)).findById(2);
    }

    @Test
    void entityToDto_success() {
        // Given
        LocalDateTime dateTime = LocalDateTime.of(2024, 1, 15, 10, 30);

        StateGameEntity state = new StateGameEntity();
        state.setId(1);
        state.setDescription("INICIADA");

        Objective objective = new Objective();
        objective.setId(2);
        objective.setDescription("Conquistar el mundo");

        Game game = new Game();
        game.setId(5);
        game.setStartDate(dateTime);
        game.setStates(state);
        game.setCommunicationType(new CommunicationType(1, "CHAT"));
        game.setObjectiveCommon(objective);

        GameDTO result = gameService.entityToDto(game);

        assertThat(result).isNotNull();
        assertThat(result.getStateId()).isEqualTo(0);
        assertThat(result.getCommonObjective()).isEqualTo(objective);
        assertThat(result.getCommonObjectiveId()).isEqualTo(0);
        assertThat(result.getState()).isEqualTo(state);
        assertThat(result.getCommunicationType()).isEqualTo(new CommunicationType(1, "CHAT"));
        assertThat(result.getDateTime()).isEqualTo(dateTime);
    }

    @Test
    void startGame_success() {
        Game game = new Game();
        game.setId(1);
        game.setCommunicationType(new CommunicationType(1, "CHAT"));
        game.setStartDate(null);

        StateGameEntity statePreparation = new StateGameEntity();
        statePreparation.setId(1);
        statePreparation.setDescription("preparacion");

        String historyMessage = "Partida iniciada correctamente";

        when(gameStateService.findByDescription("preparacion")).thenReturn(statePreparation);
        when(gameRepository.save(any(Game.class))).thenReturn(game);
        when(registerMessageEvent.startGameRegistry(game)).thenReturn(historyMessage);

        Game result = gameService.startGame(game);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getStartDate()).isNotNull();
        assertThat(result.getStates()).isEqualTo(statePreparation);
        assertThat(result.getCommunicationType()).isEqualTo(new CommunicationType(1, "CHAT"));

        verify(gameStateService, times(1)).findByDescription("preparacion");
        verify(gameRepository, times(1)).save(game);
        verify(registerMessageEvent, times(1)).startGameRegistry(game);
        verify(historyService, times(1)).registerEvent(game, historyMessage);
    }

    @Test
    void endGame_success() {
        int gameId = 1;
        Game game = createGame(gameId);

        StateGameEntity currentState = new StateGameEntity();
        currentState.setId(1);
        currentState.setDescription("INICIADA");
        game.setStates(currentState);

        StateGameEntity finishState = new StateGameEntity();
        finishState.setId(2);
        finishState.setDescription("FINISHED");

        String historyMessage = "Partida finalizada correctamente";

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(gameStateService.findByDescription("FINISHED")).thenReturn(finishState);
        when(registerMessageEvent.finishGameRegistry(game)).thenReturn(historyMessage);
        when(gameRepository.save(any(Game.class))).thenReturn(game);

        boolean result = gameService.endGame(gameId);

        assertThat(result).isTrue();
        assertThat(game.getStates()).isEqualTo(finishState);

        verify(gameRepository, times(1)).findById(gameId);
        verify(gameStateService, times(1)).findByDescription("FINISHED");
        verify(registerMessageEvent, times(1)).finishGameRegistry(game);
        verify(historyService, times(1)).registerEvent(game, historyMessage);
        verify(gameRepository, times(1)).save(game);
    }

    @Test
    void communicationStyle_success() {
        int gameId = 1;
        Game game = createGame(gameId);

        CommunicationType CommunicationType = new CommunicationType(1, "CHAT");
        game.setCommunicationType(CommunicationType);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        String result = gameService.communicationStyle(gameId);

        assertThat(result).isEqualTo("CHAT");

        verify(gameRepository, times(1)).findById(gameId);
    }

    @Test
    void moveStatePreparation() {
        // Given
        int gameId = 1;
        Game game = createGame(gameId);

        StateGameEntity statePreparation = new StateGameEntity();
        statePreparation.setId(1);
        statePreparation.setDescription("preparacion");
        game.setStates(statePreparation);

        String historyMessage = "Estado de partida avanzado";

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(registerMessageEvent.moveStateGameRegistry(gameId)).thenReturn(historyMessage);

        try (MockedConstruction<GameContext> mockedConstruction = mockConstruction(GameContext.class)) {

            gameService.moveState(gameId);

            verify(gameRepository, times(3)).findById(gameId);
            verify(registerMessageEvent, times(1)).moveStateGameRegistry(gameId);
            verify(historyService, times(1)).registerEvent(game, historyMessage);
            verify(gameRepository, times(2)).save(game);

            assertEquals(2, mockedConstruction.constructed().size());

            GameContext firstContext = mockedConstruction.constructed().get(0);
            GameContext secondContext = mockedConstruction.constructed().get(1);

            verify(firstContext, times(1)).executeTurn(game);
            verify(firstContext, times(1)).moveState(game);
            verify(secondContext, times(1)).executeTurn(game);
        }
    }


    @Test
    void moveState_FinishGame_noProgress() {
        int gameId = 1;
        Game game = createGame(gameId);

        StateGameEntity finishState = new StateGameEntity();
        finishState.setId(3);
        finishState.setDescription("FINISHED");
        game.setStates(finishState);

        String historyMessage = "Estado de partida avanzado";

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(registerMessageEvent.moveStateGameRegistry(gameId)).thenReturn(historyMessage);

        gameService.moveState(gameId);

        verify(gameRepository, times(1)).findById(gameId);
        verify(registerMessageEvent, times(1)).moveStateGameRegistry(gameId);
        verify(historyService, times(1)).registerEvent(game, historyMessage);
        verify(gameRepository, never()).save(any(Game.class));
    }

    @Test
    void getDataGame_success() {
        int gameId = 1;

        Game game = new Game();
        game.setId(gameId);
        game.setStartDate(LocalDateTime.now());

        Objective objectiveCommon = new Objective();
        objectiveCommon.setDescription("Conquistar 24 países");
        game.setObjectiveCommon(objectiveCommon);

        CommunicationType typeCom = new CommunicationType();
        typeCom.setDescription("Chat Global");
        game.setCommunicationType(typeCom);

        PlayerGame player1 = new PlayerGame();
        player1.setId(1);
        player1.setOrderTurn(2);
        PlayerGame player2 = new PlayerGame();
        player2.setId(2);
        player2.setOrderTurn(1);
        List<PlayerGame> players = Arrays.asList(player1, player2);

        PlayerGameDto playerGameDto1 = new PlayerGameDto();
        playerGameDto1.setId(1);
        PlayerGameDto playerGameDto2 = new PlayerGameDto();
        playerGameDto2.setId(2);

        CountryGame country1 = new CountryGame();
        CountryGame country2 = new CountryGame();
        List<CountryGame> countries = List.of(country1, country2);

        CountryGameDTO countryDto1 = new CountryGameDTO();
        countryDto1.setCountryId(1);
        CountryGameDTO countryDto2 = new CountryGameDTO();
        countryDto2.setCountryId(2);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(playerService.findByGameId(gameId)).thenReturn(players);
        when(playerService.PlayerGameToDTO(player2)).thenReturn(playerGameDto2); // ordenTurno 1
        when(playerService.PlayerGameToDTO(player1)).thenReturn(playerGameDto1); // ordenTurno 2

        when(countryGameService.findByGame(game)).thenReturn(countries);
        when(countryGameService.countryGameToDTO(country1)).thenReturn(countryDto1);
        when(countryGameService.countryGameToDTO(country2)).thenReturn(countryDto2);

        BigJsonDTO result = gameService.getDataGame(gameId);

        assertThat(result).isNotNull();
        assertThat(result.getGame()).isNotNull();
        assertThat(result.getGame().getCommunicationType()).isEqualTo("Chat Global");

        assertThat(result.getPlayers()).hasSize(2);
        assertThat(result.getPlayers().get(0).getId()).isEqualTo(2);
        assertThat(result.getPlayers().get(1).getId()).isEqualTo(1);

        assertThat(result.getCountries()).hasSize(2);
        assertThat(result.getCountries().get(0).getCountryId()).isEqualTo(2);
        assertThat(result.getCountries().get(1).getCountryId()).isEqualTo(2);

        verify(gameRepository).findById(gameId);
        verify(playerService).findByGameId(gameId);
        verify(countryGameService).findByGame(game);
    }


}