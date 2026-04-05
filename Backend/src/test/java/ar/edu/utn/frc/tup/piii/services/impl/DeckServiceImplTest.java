package ar.edu.utn.frc.tup.piii.services.impl;

import ar.edu.utn.frc.tup.piii.model.entities.PlayerGame;
import ar.edu.utn.frc.tup.piii.model.entities.Game;
import ar.edu.utn.frc.tup.piii.model.entities.CardCountry;
import ar.edu.utn.frc.tup.piii.model.entities.CardPlayer;
import ar.edu.utn.frc.tup.piii.model.repository.CardCountryRepository;
import ar.edu.utn.frc.tup.piii.model.repository.CardPlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeckServiceImplTest {

    @Mock
    private CardCountryRepository countryRepository;
    @Mock
    private CardPlayerRepository playerRepository;
    @InjectMocks
    private DeckServiceImpl deckService;

    private CardCountry t1, t2;
    private CardPlayer tj1;

    @BeforeEach
    void setUp() {
        t1 = new CardCountry(); t1.setId(1);
        t2 = new CardCountry(); t2.setId(2);
        tj1 = new CardPlayer();
        tj1.setCardCountry(t1);
    }

    @Test
    void initDeck_withData_NotEmpty() {
        when(countryRepository.findAll()).thenReturn(Arrays.asList(t1, t2));

        deckService.initDeck();

        assertFalse(deckService.isEmptyDeck());
    }

    @Test
    void initDeck_withData_isEmpty() {
        when(countryRepository.findAll()).thenReturn(Collections.emptyList());

        deckService.initDeck();

        assertTrue(deckService.isEmptyDeck());
    }

    @Test
    void countAvailableCards_filterAssigned() {
        when(countryRepository.findAll()).thenReturn(Arrays.asList(t1, t2));
        when(playerRepository.findAll()).thenReturn(Collections.singletonList(tj1));

        deckService.initDeck();
        assertEquals(1, deckService.countAvailableCards());
    }

    @Test
    void getAvailableCards_returnNotNull() {
        when(countryRepository.findAll()).thenReturn(Arrays.asList(t1, t2));
        when(playerRepository.findAll()).thenReturn(Collections.emptyList());

        deckService.initDeck();
        CardCountry result = deckService.getAvailableCards();
        assertNotNull(result);
    }

    @Test
    void initDeckToGame_withData_notEmptyToGame() {
        when(countryRepository.findAll()).thenReturn(Arrays.asList(t1, t2));

        deckService.initDeckToGame(42);
        assertFalse(deckService.isEmptyDeckToGame(42));
    }

    @Test
    void returnCardToDeck_nullDoesNothing() {
        deckService.returnCardToDeck(null);
        verify(playerRepository, never()).findAll();
    }

    @Test
    void getAvailableCards_deckNull_returnNull() {
        CardCountry result = deckService.getAvailableCards();
        assertNull(result);
    }

    @Test
    void getAvailableCards_withoutAvailableAfterRestart_returnNull() {

        when(countryRepository.findAll()).thenReturn(Arrays.asList(t1, t2));

        when(playerRepository.findAll()).thenReturn(Arrays.asList(
                new CardPlayer() {{ setCardCountry(t1); }},
                new CardPlayer() {{ setCardCountry(t2); }}
        ));

        deckService.initDeck();

        CardCountry result = deckService.getAvailableCards();

        assertNull(result);
    }

    @Test
    void getAvailableCardsToGame_withoutInit_returnNull() {
        CardCountry result = deckService.getAvailableCardsToGame(99);
        assertNull(result);
    }

    @Test
    void getAvailableCardsToGame_withoutAvailableAfterRestart_returnNull() {
        int gameId = 88;

        when(countryRepository.findAll()).thenReturn(Arrays.asList(t1, t2));
        deckService.initDeckToGame(gameId);

        Game gameMock = new Game(); gameMock.setId(gameId);

        PlayerGame playerGame1 = new PlayerGame();
        playerGame1.setGame(gameMock);
        CardPlayer tj1 = new CardPlayer();
        tj1.setCardCountry(t1);
        tj1.setPlayerGame(playerGame1);

        PlayerGame playerGame2 = new PlayerGame();
        playerGame2.setGame(gameMock);
        CardPlayer tj2 = new CardPlayer();
        tj2.setCardCountry(t2);
        tj2.setPlayerGame(playerGame2);

        when(playerRepository.findAll()).thenReturn(List.of(tj1, tj2));

        CardCountry result = deckService.getAvailableCardsToGame(gameId);

        assertNull(result);
    }

    @Test
    void shuttleDeckFull_restartIndex() {

        when(countryRepository.findAll()).thenReturn(List.of(t1, t2));
        deckService.initDeck();

        deckService.shuttleDeck();

        assertNotNull(deckService.getAvailableCards());
    }

    @Test
    void shuttleDeckEmpty_notThrowException() {
        assertDoesNotThrow(() -> deckService.shuttleDeck());
    }

    @Test
    void getAvailableCardsToGame_deckNull_returnNull() {
        int gameId = 99;

        CardCountry result = deckService.getAvailableCardsToGame(gameId);

        assertNull(result);
    }

    @Test
    void getCardsInDeckNull_returnEmptyList() {
        List<CardCountry> result = deckService.getCardsInDeck();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getCardsInDeck_withAvailableCards_returnsIt() {
        when(countryRepository.findAll()).thenReturn(List.of(t1, t2));
        when(playerRepository.findAll()).thenReturn(List.of(tj1));

        deckService.initDeck();
        List<CardCountry> result = deckService.getCardsInDeck();

        assertEquals(1, result.size());
        assertEquals(t2, result.get(0));
    }



}
