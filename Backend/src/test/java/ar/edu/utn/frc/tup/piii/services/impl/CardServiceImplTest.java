package ar.edu.utn.frc.tup.piii.services.impl;

import ar.edu.utn.frc.tup.piii.mappers.BasePlayerMapper;
import ar.edu.utn.frc.tup.piii.mappers.PlayerMapper;
import ar.edu.utn.frc.tup.piii.model.entities.*;
import ar.edu.utn.frc.tup.piii.model.repository.PlayerRepository;
import ar.edu.utn.frc.tup.piii.model.repository.CardPlayerRepository;
import ar.edu.utn.frc.tup.piii.model.repository.CardCountryRepository;
import ar.edu.utn.frc.tup.piii.services.interfaces.*;
import ar.edu.utn.frc.tup.piii.util.RegisterMessageEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

    @Mock
    CardCountryRepository cardCountryRepository;

    @Mock
    CardPlayerRepository cardPlayerRepository;

    @Mock
    PlayerService playerService;

    @Mock
    CardExchangeService cardExchangeService;

    @Mock
    CountryGameService countryGameService;

    @Mock
    PlayerMapper playerMapper;

    @Mock
    BasePlayerMapper basePlayerMapper;

    @Mock
    PlayerRepository playerRepository;

    @Mock
    RegisterMessageEvent registerMessageEvent;

    @Mock
    HistoryService historyService;

    @Mock
    DeckService deckService;

    @InjectMocks
    CardServiceImpl cardService;

    PlayerGame playerGame;
    CardCountry cardCountry;

    @BeforeEach
    void setup() {
        playerGame = new PlayerGame();
        playerGame.setId(1);
        Game game = new Game();
        game.setId(10);
        playerGame.setGame(game);

        cardCountry = new CardCountry();
        cardCountry.setId(100);
        Country country = new Country();
        country.setName("Argentine");
        cardCountry.setCountry(country);
    }

    @Test
    void assignCountryCardTest() {
        BasePlayer basePlayer = Mockito.mock(BasePlayer.class);
        when(basePlayer.getName()).thenReturn("Santiago");
        playerGame.setPlayer(basePlayer);

        when(cardCountryRepository.findById(100)).thenReturn(Optional.of(cardCountry));
        when(playerService.findPlayerGameById(1)).thenReturn(Optional.of(playerGame));
        when(cardPlayerRepository.save(any(CardPlayer.class))).thenAnswer(i -> i.getArgument(0));

        CardCountry result = cardService.assignCardToPlayer(100, 1);

        assertNotNull(result);
        assertEquals("Argentine", result.getCountry().getName());
        verify(cardPlayerRepository).save(any(CardPlayer.class));
        verify(historyService).registerEvent(eq(playerGame.getGame()), anyString());
    }



    @Test
    void askCountryCardTest() {
        when(playerService.findPlayerGameById(1)).thenReturn(Optional.of(playerGame));
        cardService.markConquer(1);

        CardCountry availableCard = new CardCountry();
        Country country = new Country();
        country.setName("Brazil");
        availableCard.setCountry(country);
        availableCard.setId(200);

        CardServiceImpl spyService = Mockito.spy(cardService);
        doReturn(availableCard).when(spyService).getAvailableCard();
        doReturn(availableCard).when(spyService).assignCardToPlayer(200, 1);

        spyService.askCard(1);

        verify(spyService).assignCardToPlayer(200, 1);
        verify(spyService).cleanState(1);
    }

    @Test
    void gaveArmyTest() {
        BasePlayer basePlayer = mock(BasePlayer.class);
        basePlayer.setAvailableArmies(1);

        playerGame.setPlayer(basePlayer);

        CardPlayer cardPlayer = new CardPlayer();
        cardPlayer.setId(300);
        cardPlayer.setUsed(false);
        cardPlayer.setCardCountry(cardCountry);

        when(playerService.findPlayerGameById(1)).thenReturn(Optional.of(playerGame));
        when(cardPlayerRepository.findById(300)).thenReturn(Optional.of(cardPlayer));
        when(countryGameService.findByGameAndPlayerGame(anyInt(), anyInt())).thenReturn(
                List.of(new CountryGame() {{
                    setCountry(cardCountry.getCountry());
                }})
        );

        cardService.giveArmiesWithCards(10, 1, 300);

        assertTrue(cardPlayer.isUsed());
        verify(cardPlayerRepository).save(cardPlayer);
        verify(playerService).saveBasePlayer(basePlayer);
        verify(historyService).registerEvent(eq(playerGame.getGame()), anyString());
    }

    @Test
    void gaveArmiesUsedCardTest() {
        CardPlayer cardPlayer = new CardPlayer();
        cardPlayer.setUsed(true);

        when(cardPlayerRepository.findById(300)).thenReturn(Optional.of(cardPlayer));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                cardService.giveArmiesWithCards(10, 1, 300));
        assertEquals("Esta tarjeta ya fue usada.", ex.getMessage());
    }

    @Test
    void gaveArmiesNotCountryTest() {
        CardPlayer cardPlayer = new CardPlayer();
        cardPlayer.setUsed(false);
        cardPlayer.setCardCountry(cardCountry);

        when(cardPlayerRepository.findById(300)).thenReturn(Optional.of(cardPlayer));
        when(playerService.findPlayerGameById(1)).thenReturn(Optional.of(playerGame));
        when(countryGameService.findByGameAndPlayerGame(anyInt(), anyInt())).thenReturn(List.of());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                cardService.giveArmiesWithCards(10, 1, 300));
        assertEquals(cardCountry.getCountry().getName(), ex.getMessage());
    }

    @Test
    void assignCardSuccessTest() {
        int cardId = 1;
        int playerGameId = 1;

        Country country = new Country();
        country.setName("Argentine");
        CardCountry cardCountry = new CardCountry();
        cardCountry.setId(cardId);
        cardCountry.setCountry(country);

        BasePlayer basePlayer = Mockito.mock(BasePlayer.class);
        PlayerGame playerGame = new PlayerGame();
        playerGame.setId(playerGameId);
        playerGame.setPlayer(basePlayer);
        playerGame.setGame(new Game());

        Mockito.when(cardCountryRepository.findById(cardId)).thenReturn(Optional.of(cardCountry));
        Mockito.when(playerService.findPlayerGameById(playerGameId)).thenReturn(Optional.of(playerGame));
        Mockito.when(cardPlayerRepository.save(Mockito.any())).thenAnswer(i -> i.getArgument(0));

        CardCountry result = cardService.assignCardToPlayer(cardId, playerGameId);

        Assertions.assertEquals(cardCountry, result);
        Mockito.verify(cardPlayerRepository).save(Mockito.any(CardPlayer.class));
        Mockito.verify(historyService).registerEvent(Mockito.eq(playerGame.getGame()), Mockito.anyString());
    }

    @Test
    void askCardNotConquerTest() {
        int playerId = 1;

        PlayerGame playerGameMock = new PlayerGame();
        playerGameMock.setId(playerId);
        playerGameMock.setPlayer(Mockito.mock(BasePlayer.class));
        playerGameMock.setGame(new Game());

        Mockito.lenient().when(playerService.findPlayerGameById(playerId)).thenReturn(Optional.of(playerGameMock));

        cardService.cleanState(playerId);

        CardServiceImpl spyService = Mockito.spy(cardService);

        spyService.askCard(playerId);

        Mockito.verify(spyService, Mockito.never()).assignCardToPlayer(Mockito.anyInt(), Mockito.anyInt());
    }


    @Test
    void giveArmiesWithCardsUsedTest() {
        int gameId = 1;
        int playerGameId = 1;
        int cardId = 10;

        CardPlayer cardPlayer = new CardPlayer();
        cardPlayer.setId(cardId);
        cardPlayer.setUsed(true);

        Mockito.when(cardPlayerRepository.findById(cardId)).thenReturn(Optional.of(cardPlayer));

        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            cardService.giveArmiesWithCards(gameId, playerGameId, cardId);
        });

        Assertions.assertEquals("Esta tarjeta ya fue usada.", ex.getMessage());
    }




    @Test
    void giveArmiesWithCardsNoCountryTest() {
        int gameId = 1;
        int playerGameId = 1;
        int cardId = 10;

        Country country = new Country();
        country.setName("Chili");

        CardCountry cardCountry = new CardCountry();
        cardCountry.setCountry(country);

        CardPlayer cardPlayer = new CardPlayer();
        cardPlayer.setId(cardId);
        cardPlayer.setUsed(false);
        cardPlayer.setCardCountry(cardCountry);

        PlayerGame playerGame = new PlayerGame();
        playerGame.setId(playerGameId);
        playerGame.setPlayer(new BasePlayer() {});

        Mockito.when(playerService.findPlayerGameById(playerGameId)).thenReturn(Optional.of(playerGame));
        Mockito.when(cardPlayerRepository.findById(cardId)).thenReturn(Optional.of(cardPlayer));
        Mockito.when(countryGameService.findByGameAndPlayerGame(gameId, playerGameId))
                .thenReturn(List.of());

        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            cardService.giveArmiesWithCards(gameId, playerGameId, cardId);
        });

        Assertions.assertEquals("Chili", ex.getMessage());
    }

    @Test
    void getCardWithoutAssignTest() {
        CardCountry t1 = new CardCountry();
        t1.setId(1);
        CardCountry t2 = new CardCountry();
        t2.setId(2);

        when(cardPlayerRepository.findAll()).thenReturn(List.of());
        when(cardCountryRepository.findAll()).thenReturn(List.of(t1, t2));

        CardCountry result = cardService.getAvailableCard();

        assertEquals(t1, result);
    }

    @Test
    void getAvailableCardTest() {
        CardCountry t1 = new CardCountry();
        t1.setId(1);
        CardCountry t2 = new CardCountry();
        t2.setId(2);

        CardPlayer tj = new CardPlayer();
        CardCountry assignedCard = new CardCountry();
        assignedCard.setId(1);
        tj.setCardCountry(assignedCard);

        when(cardPlayerRepository.findAll()).thenReturn(List.of(tj));
        when(cardCountryRepository.findAll()).thenReturn(List.of(t1, t2));

        CardCountry result = cardService.getAvailableCard();

        assertEquals(t2, result);
    }

    @Test
    void markCardAsUsedTest() {
        CardPlayer cardPlayer = new CardPlayer();
        cardPlayer.setId(1);
        cardPlayer.setUsed(false);

        when(cardPlayerRepository.findById(1)).thenReturn(Optional.of(cardPlayer));

        cardService.markCardAsUsed(1);

        assertTrue(cardPlayer.isUsed());
        verify(cardPlayerRepository).save(cardPlayer);
    }

    @Test
    void markCardAsUsedDoNotExistsTest() {
        when(cardPlayerRepository.findById(999)).thenReturn(Optional.empty());

        cardService.markCardAsUsed(999);

        verify(cardPlayerRepository, never()).save(any());
    }

    @Test
    void getPlayerCardsTest() {
        CardPlayer card1 = new CardPlayer();
        CardPlayer card2 = new CardPlayer();

        when(cardPlayerRepository.findByPlayerGame_Id(1)).thenReturn(List.of(card1, card2));

        List<CardPlayer> result = cardService.getPlayerCards(1);

        assertEquals(2, result.size());
    }

    @Test
    void isValidExchangeTest() {
        List<Integer> ids = List.of(1, 2, 3);
        when(cardExchangeService.isValidExchange(ids, 1)).thenReturn(true);

        boolean valid = cardService.isValidExchange(ids, 1);

        assertTrue(valid);
    }

    @Test
    void removePlayerCardTest() {
        CardPlayer t1 = new CardPlayer();
        CardPlayer t2 = new CardPlayer();

        List<CardPlayer> cards = List.of(t1, t2);

        cardService.deletePlayerCards(cards);

        verify(cardPlayerRepository).deleteAll(cards);
    }

    @Test
    void saveCardsTest() {
        CardPlayer t1 = new CardPlayer();
        CardPlayer t2 = new CardPlayer();

        List<CardPlayer> cards = List.of(t1, t2);

        cardService.saveAll(cards);

        verify(cardPlayerRepository).saveAll(cards);
    }

    @Test
    void updateCountry_existent_returnTrue() {
        CardCountry card = new CardCountry();
        card.setId(1);

        when(cardCountryRepository.existsById(1)).thenReturn(true);
        when(cardCountryRepository.save(card)).thenReturn(card);

        boolean result = cardService.updateCountry(card);

        assertTrue(result);
        verify(cardCountryRepository).save(card);
    }

    @Test
    void updateCountry_noExistent_returnFalse() {
        CardCountry card = new CardCountry();
        card.setId(99);

        when(cardCountryRepository.existsById(99)).thenReturn(false);

        boolean result = cardService.updateCountry(card);

        assertFalse(result);
        verify(cardCountryRepository, never()).save(any());
    }

    @Test
    void findByIdCountry_existent_returnCard() {
        CardCountry card = new CardCountry();
        card.setId(5);

        when(cardCountryRepository.findById(5)).thenReturn(Optional.of(card));

        CardCountry result = cardService.findByIdCountry(5);

        assertNotNull(result);
        assertEquals(5, result.getId());
    }

    @Test
    void findByIdCountry_inexistent_returnNull() {
        when(cardCountryRepository.findById(999)).thenReturn(Optional.empty());

        CardCountry result = cardService.findByIdCountry(999);

        assertNull(result);
    }

    @Test
    void findAllCountry_returnList() {
        List<CardCountry> list = List.of(new CardCountry(), new CardCountry());
        when(cardCountryRepository.findAll()).thenReturn(list);

        List<CardCountry> result = cardService.findAllCountry();

        assertEquals(2, result.size());
    }

    @Test
    void savePlayer_valid_returnTrue() {
        CardPlayer card = new CardPlayer();
        card.setId(10);

        when(cardPlayerRepository.save(card)).thenReturn(card);

        boolean result = cardService.savePlayer(card);

        assertTrue(result);
    }

    @Test
    void savePlayer_idInvalid_returnFalse() {
        CardPlayer cardPlayer = new CardPlayer();
        cardPlayer.setId(0); // ID inválido

        when(cardPlayerRepository.save(cardPlayer)).thenReturn(cardPlayer);

        boolean result = cardService.savePlayer(cardPlayer);

        assertFalse(result);
    }

    @Test
    void updatePlayer_existent_returnTrue() {
        CardPlayer card = new CardPlayer();
        card.setId(1);

        when(cardPlayerRepository.existsById(1)).thenReturn(true);
        when(cardPlayerRepository.save(card)).thenReturn(card);

        boolean result = cardService.updatePlayer(card);

        assertTrue(result);
    }

    @Test
    void updatePlayer_noExistent_returnFalse() {
        CardPlayer cardPlayer = new CardPlayer();
        cardPlayer.setId(88);

        when(cardPlayerRepository.existsById(88)).thenReturn(false);

        boolean result = cardService.updatePlayer(cardPlayer);

        assertFalse(result);
        verify(cardPlayerRepository, never()).save(any());
    }

    @Test
    void findByIdPlayer_existent_returnCard() {
        CardPlayer card = new CardPlayer();
        card.setId(7);

        when(cardPlayerRepository.findById(7)).thenReturn(Optional.of(card));

        CardPlayer result = cardService.findByIdPlayer(7);

        assertNotNull(result);
        assertEquals(7, result.getId());
    }

    @Test
    void findByIdPlayer_inexistent_returnNull() {
        when(cardPlayerRepository.findById(123)).thenReturn(Optional.empty());

        CardPlayer result = cardService.findByIdPlayer(123);

        assertNull(result);
    }

    @Test
    void findAllPlayer_returnList() {
        List<CardPlayer> list = List.of(new CardPlayer(), new CardPlayer(), new CardPlayer());
        when(cardPlayerRepository.findAll()).thenReturn(list);

        List<CardPlayer> result = cardService.findAllPlayer();

        assertEquals(3, result.size());
    }



}
