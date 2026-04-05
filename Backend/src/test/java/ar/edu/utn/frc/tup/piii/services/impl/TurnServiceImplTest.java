package ar.edu.utn.frc.tup.piii.services.impl;

import ar.edu.utn.frc.tup.piii.model.entities.*;
import ar.edu.utn.frc.tup.piii.model.enums.TurnPhase;
import ar.edu.utn.frc.tup.piii.model.repository.TurnRepository;
import ar.edu.utn.frc.tup.piii.services.interfaces.HistoryService;
import ar.edu.utn.frc.tup.piii.services.interfaces.PlayerService;
import ar.edu.utn.frc.tup.piii.services.interfaces.CountryGameService;
import ar.edu.utn.frc.tup.piii.services.interfaces.GameService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class TurnServiceImplTest {

    @Mock
    private TurnRepository turnRepository;
    @Mock
    private PlayerService playerService;
    @Mock
    private GameService gameService;
    @Mock
    private CountryGameService countryGameService;
    @Mock
    private HistoryService historyService;

    @Spy
    @InjectMocks
    private TurnServiceImpl turnServiceSpy;

    @InjectMocks
    private TurnServiceImpl turnService;

    @Test
    void startTurnTest() {
        BasePlayer player = new BasePlayer() {
        };
        player.setAvailableArmies(10);

        PlayerGame playerGame = new PlayerGame();
        playerGame.setPlayer(player);
        playerGame.setTurn(false);

        Game game = new Game();

        Turn turnMock = new Turn();
        turnMock.setId(1);
        Mockito.when(turnRepository.save(Mockito.any(Turn.class))).thenReturn(turnMock);

        turnService.startTurn(playerGame, game);

        Mockito.verify(turnRepository).save(Mockito.argThat(turn ->
                turn.getPlayerGame() == playerGame &&
                        turn.getCurrentPhase() == TurnPhase.INCORPORATION &&
                        turn.getGame() == game &&
                        turn.getAvailableArmies() == 10
        ));

        Mockito.verify(historyService).registerEvent(Mockito.eq(game), Mockito.anyString());
        Assertions.assertTrue(playerGame.isTurn());
    }

    @Test
    void movePhaseTurnTest() {
        Turn turn = new Turn();
        turn.setCurrentPhase(TurnPhase.INCORPORATION);

        Game game = new Game();
        turn.setGame(game);

        BasePlayer player = new HumanPlayer();
        PlayerGame jp = new PlayerGame();
        jp.setPlayer(player);
        turn.setPlayerGame(jp);

        Mockito.when(turnRepository.save(Mockito.any())).thenReturn(turn);

        turnService.movePhase(turn);

        Assertions.assertEquals(TurnPhase.ATTACK, turn.getCurrentPhase());
        Mockito.verify(historyService).registerEvent(Mockito.eq(game), Mockito.anyString());
    }

    @Test
    void putArmySuccessTest() {
        int playerId = 1;
        int countryId = 10;
        int gameId = 100;
        int quantity = 3;


        BasePlayer basePlayer = Mockito.mock(BasePlayer.class);
        Mockito.when(basePlayer.getAvailableArmies()).thenReturn(5);

        PlayerGame playerGame = new PlayerGame();
        playerGame.setId(playerId);
        playerGame.setPlayer(basePlayer);
        Game game = new Game();
        game.setId(gameId);
        playerGame.setGame(game);

        Mockito.when(playerService.findPlayerGameById(playerId)).thenReturn(Optional.of(playerGame));

        CountryGame country = new CountryGame();
        country.setId(new CountryGameId(countryId, gameId));
        country.setAmountArmies(2);
        country.setPlayerGame(playerGame);

        Country countryEntity = new Country();
        countryEntity.setName("Argentine");
        country.setCountry(countryEntity);

        Mockito.when(countryGameService.findById(countryId, gameId)).thenReturn(country);


        turnService.putArmy(playerId, countryId, quantity);

        Mockito.verify(countryGameService).save(country);
        Mockito.verify(playerService).update(basePlayer);
        Mockito.verify(historyService).registerEvent(Mockito.eq(game), Mockito.anyString());
    }

    @Test
    void putArmyNoHasTest() {
        int playerId = 1;
        int countryId = 10;
        int gameId = 100;
        int quantity = 5;

        BasePlayer basePlayer = Mockito.mock(BasePlayer.class);
        Mockito.when(basePlayer.getAvailableArmies()).thenReturn(2);

        PlayerGame playerGame = new PlayerGame();
        playerGame.setId(playerId);
        playerGame.setPlayer(basePlayer);
        Game game = new Game();
        game.setId(gameId);
        playerGame.setGame(game);

        Mockito.when(playerService.findPlayerGameById(playerId)).thenReturn(Optional.of(playerGame));

        CountryGame country = new CountryGame();
        country.setId(new CountryGameId(countryId, gameId));
        country.setAmountArmies(3);
        country.setPlayerGame(playerGame);

        Country countryEntity = new Country();
        countryEntity.setName("Brazil");
        country.setCountry(countryEntity);


        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> turnService.putArmy(playerId, countryId, quantity)
        );

        Assertions.assertEquals("No tiene suficientes ejércitos disponibles", thrown.getMessage());

        Mockito.verify(countryGameService, Mockito.never()).save(Mockito.any());
        Mockito.verify(playerService, Mockito.never()).update(Mockito.any());
        Mockito.verify(historyService, Mockito.never()).registerEvent(Mockito.any(), Mockito.any());
    }


    @Test
    void putArmyNoCountryTest() {
        int playerId = 1;
        int anotherPlayerId = 2;
        int countryId = 10;
        int gameId = 100;
        int quantity = 3;

        BasePlayer basePlayer = Mockito.mock(BasePlayer.class);
        Mockito.when(basePlayer.getAvailableArmies()).thenReturn(10);

        PlayerGame playerGame = new PlayerGame();
        playerGame.setId(playerId);
        playerGame.setPlayer(basePlayer);
        Game game = new Game();
        game.setId(gameId);
        playerGame.setGame(game);

        Mockito.when(playerService.findPlayerGameById(playerId)).thenReturn(Optional.of(playerGame));

        PlayerGame anotherPlayer = new PlayerGame();
        anotherPlayer.setId(anotherPlayerId);

        CountryGame country = new CountryGame();
        country.setId(new CountryGameId(countryId, gameId));
        country.setAmountArmies(5);
        country.setPlayerGame(anotherPlayer);

        Country countryEntity = new Country();
        countryEntity.setName("Chili");
        country.setCountry(countryEntity);

        Mockito.when(countryGameService.findById(countryId, gameId)).thenReturn(country);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> turnService.putArmy(playerId, countryId, quantity)
        );

        Assertions.assertEquals("El país no pertenece al jugador", ex.getMessage());

        Mockito.verify(countryGameService, Mockito.never()).save(Mockito.any());
        Mockito.verify(playerService, Mockito.never()).update(Mockito.any());
        Mockito.verify(historyService, Mockito.never()).registerEvent(Mockito.any(), Mockito.any());
    }


    @Test
    void finishRoundTest() {
        int playerId = 1;
        int gameId = 100;

        BasePlayer currentPlayer = Mockito.mock(BasePlayer.class);
        PlayerGame playerGameCurrent = new PlayerGame();
        playerGameCurrent.setId(playerId);
        playerGameCurrent.setPlayer(currentPlayer);
        playerGameCurrent.setOrderTurn(1);
        playerGameCurrent.setActive(true);

        Game game = new Game();
        game.setId(gameId);
        playerGameCurrent.setGame(game);

        PlayerGame player2 = new PlayerGame();
        player2.setId(2);
        player2.setOrderTurn(2);
        player2.setActive(true);
        player2.setGame(game);

        PlayerGame player3 = new PlayerGame();
        player3.setId(3);
        player3.setOrderTurn(3);
        player3.setActive(true);
        player3.setGame(game);

        List<PlayerGame> players = List.of(playerGameCurrent, player2, player3);

        Mockito.when(playerService.findPlayerGameById(playerId)).thenReturn(Optional.of(playerGameCurrent));
        Mockito.when(playerService.findByGameId(gameId)).thenReturn(players);

        Mockito.doNothing().when(turnServiceSpy).startTurn(Mockito.any(), Mockito.any());

        assertThrows(
                NullPointerException.class,
                () -> {
                    turnServiceSpy.finishTurnRound(playerId, new Turn());
                }
        );
    }

    @Test
    void finishRoundMoveStateTest() {
        int playerId = 3;
        int gameId = 100;

        BasePlayer currentPlayer = Mockito.mock(BasePlayer.class);
        PlayerGame playerGameActual = new PlayerGame();
        playerGameActual.setId(playerId);
        playerGameActual.setPlayer(currentPlayer);
        playerGameActual.setOrderTurn(3);
        playerGameActual.setActive(true);

        Game game = new Game();
        game.setId(gameId);
        playerGameActual.setGame(game);

        PlayerGame player1 = new PlayerGame();
        player1.setId(1);
        player1.setOrderTurn(1);
        player1.setActive(true);
        player1.setGame(game);

        PlayerGame player2 = new PlayerGame();
        player2.setId(2);
        player2.setOrderTurn(2);
        player2.setActive(true);
        player2.setGame(game);

        List<PlayerGame> players = List.of(player1, player2, playerGameActual);

        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    turnServiceSpy.finishTurnRound(playerId, new Turn());
                }
        );
    }

    @Test
    void moveArmiesTest() {
        // IDs
        int countryIdOrigin = 10;
        int countryIdDestination = 20;
        int gameId = 100;
        int troopsToMove = 3;

        PlayerGame player = new PlayerGame();
        player.setId(1);
        player.setActive(true);

        Game game = new Game();
        game.setId(gameId);

        player.setGame(game);

        CountryGame origin = new CountryGame();
        origin.setId(new CountryGameId(countryIdOrigin, gameId));
        origin.setAmountArmies(5);
        origin.setPlayerGame(player);

        CountryGame destine = new CountryGame();
        destine.setId(new CountryGameId(countryIdDestination, gameId));
        destine.setAmountArmies(2);
        destine.setPlayerGame(player);

        Mockito.when(countryGameService.findById(countryIdOrigin, gameId)).thenReturn(origin);
        Mockito.when(countryGameService.findById(countryIdDestination, gameId)).thenReturn(destine);

        turnService.moveArmies(
                new CountryGameId(countryIdOrigin, gameId),
                new CountryGameId(countryIdDestination, gameId),
                troopsToMove
        );

        Assertions.assertEquals(2, origin.getAmountArmies());
        Assertions.assertEquals(5, destine.getAmountArmies());

        Mockito.verify(countryGameService).save(origin);
        Mockito.verify(countryGameService).save(destine);
    }

    @Test
    void moveArmiesInsufficientTest() {
        int originCountryId = 10;
        int destineCountryId = 20;
        int gameId = 100;
        int troopsToMove = 5;

        PlayerGame playerGame = new PlayerGame();
        playerGame.setId(1);
        playerGame.setActive(true);

        Game game = new Game();
        game.setId(gameId);
        playerGame.setGame(game);

        CountryGame origin = new CountryGame();
        origin.setId(new CountryGameId(originCountryId, gameId));
        origin.setAmountArmies(5);
        origin.setPlayerGame(playerGame);

        CountryGame destine = new CountryGame();
        destine.setId(new CountryGameId(destineCountryId, gameId));
        destine.setAmountArmies(2);
        destine.setPlayerGame(playerGame);

        Mockito.when(countryGameService.findById(originCountryId, gameId)).thenReturn(origin);
        Mockito.when(countryGameService.findById(destineCountryId, gameId)).thenReturn(destine);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            turnService.moveArmies(
                    new CountryGameId(originCountryId, gameId),
                    new CountryGameId(destineCountryId, gameId),
                    troopsToMove
            );
        });

        Assertions.assertEquals("No se puede dejar el país sin al menos un ejército", ex.getMessage());
    }

    @Test
    void getActivePlayerTest() {
        int gameId = 100;

        PlayerGame playerGame = new PlayerGame();
        playerGame.setId(5);

        Turn turn = new Turn();
        turn.setPlayerGame(playerGame);

        Mockito.when(turnRepository.findByGame_IdAndFinishedFalse(gameId)).thenReturn(Optional.of(turn));

        int playerInTurn = turnService.getPlayerInTurn(gameId);

        Assertions.assertEquals(5, playerInTurn);
    }

    @Test
    void getPlayerInactiveTest() {
        int gameId = 100;

        Mockito.when(turnRepository.findByGame_IdAndFinishedFalse(gameId)).thenReturn(Optional.empty());

        NoSuchElementException ex = assertThrows(NoSuchElementException.class, () -> {
            turnService.getPlayerInTurn(gameId);
        });

        Assertions.assertEquals("No se pudo encontrar el turno con la partida especificada.", ex.getMessage());
    }

    @Test
    void getTurnTest() {
        int turnId = 1;
        Turn turn = new Turn();
        turn.setId(turnId);

        Mockito.when(turnRepository.findById(turnId)).thenReturn(Optional.of(turn));

        Turn result = turnService.findById(turnId);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(turnId, result.getId());
    }

    @Test
    void getTurnDoNotExistsTest() {
        int turnId = 999;

        Mockito.when(turnRepository.findById(turnId)).thenReturn(Optional.empty());

        Turn result = turnService.findById(turnId);

        Assertions.assertNull(result);
    }

    @Test
    void returnListTurnsTest() {
        List<Turn> turns = List.of(new Turn(), new Turn());

        Mockito.when(turnRepository.findAll()).thenReturn(turns);

        List<Turn> result = turnService.findAll();

        Assertions.assertEquals(2, result.size());
    }



    @Test
    void getActionsTest() {
        Turn turn = new Turn();
        turn.setCurrentPhase(TurnPhase.ATTACK);

        Game game = new Game();
        game.setStates(new StateGameEntity());
        game.getStates().setDescription("HOSTILITIES");

        turn.setGame(game);

        List<String> actions = turnService.getAvailableActions(turn);

        Assertions.assertTrue(actions.contains("attack"));
        Assertions.assertTrue(actions.contains("askCard"));
        Assertions.assertTrue(actions.contains("requestArmies"));
        Assertions.assertTrue(actions.contains("exchange"));
    }















}