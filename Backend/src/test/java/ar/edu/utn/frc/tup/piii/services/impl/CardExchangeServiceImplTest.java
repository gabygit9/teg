package ar.edu.utn.frc.tup.piii.services.impl;

import ar.edu.utn.frc.tup.piii.model.entities.*;
import ar.edu.utn.frc.tup.piii.model.repository.ExchangeRepository;
import ar.edu.utn.frc.tup.piii.model.repository.ExchangeCardsRepository;
import ar.edu.utn.frc.tup.piii.services.interfaces.PlayerService;
import ar.edu.utn.frc.tup.piii.services.interfaces.CardService;
import ar.edu.utn.frc.tup.piii.services.interfaces.HistoryService;
import ar.edu.utn.frc.tup.piii.util.RegisterMessageEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardExchangeServiceImplTest {

    @Mock
    CardService cardService;
    @Mock
    PlayerService playerService;
    @Mock
    ExchangeRepository exchangeRepository;
    @Mock
    ExchangeCardsRepository exchangeCardsRepository;
    @Mock
    HistoryService historyService;
    @Mock
    RegisterMessageEvent registerMessageEvent;
    @InjectMocks
    CardExchangeServiceImpl cardExchangeService;

    // datos comunes para tests
    CardPlayer t1, t2, t3;
    List<CardPlayer> threeCards;

    @BeforeEach
    void setUp() throws Exception {
        // resetear el contador interno
        Field f = CardExchangeServiceImpl.class
                .getDeclaredField("countExchangesPlayer");
        f.setAccessible(true);
        ((Map<?,?>)f.get(cardExchangeService)).clear();

        // crear 3 tarjetas no usadas del mismo jugadorPartidaId=1
        PlayerGame jp = new PlayerGame();
        jp.setId(1);

        t1 = new CardPlayer(); t1.setId(1); t1.setUsed(false); t1.setPlayerGame(jp);
        CardCountry tp1 = new CardCountry(); tp1.setSymbol(new Symbol(1,"")); t1.setCardCountry(tp1);

        t2 = new CardPlayer(); t2.setId(2); t2.setUsed(false); t2.setPlayerGame(jp);
        CardCountry tp2 = new CardCountry(); tp2.setSymbol(new Symbol(2,"")); t2.setCardCountry(tp2);

        t3 = new CardPlayer(); t3.setId(3); t3.setUsed(false); t3.setPlayerGame(jp);
        CardCountry tp3 = new CardCountry(); tp3.setSymbol(new Symbol(3,"")); t3.setCardCountry(tp3);

        threeCards = List.of(t1, t2, t3);
    }

    // save(...)
    @Test
    void save_zeroIdsAndNonZero() {
        CardsExchange ct = new CardsExchange();
        // caso canjeId = 0
        ct.setId(new CardsExchangeId(0, 5));
        when(exchangeCardsRepository.save(ct)).thenReturn(ct);
        assertFalse(cardExchangeService.save(ct));

        // caso tarjetaJugadorId = 0
        ct.setId(new CardsExchangeId(7, 0));
        assertFalse(cardExchangeService.save(ct));

        ct.setId(new CardsExchangeId(7, 8));
        when(exchangeCardsRepository.save(ct)).thenReturn(ct);
        assertTrue(cardExchangeService.save(ct));
    }


    @Test
    void findById_presentAndNotFound() {
        CardsExchange ct = new CardsExchange();
        when(exchangeCardsRepository.findById(5)).thenReturn(Optional.of(ct));
        assertSame(ct, cardExchangeService.findById(5));
        when(exchangeCardsRepository.findById(6)).thenReturn(Optional.empty());
        assertNull(cardExchangeService.findById(6));
    }


    @Test
    void findAll_returnsList() {
        List<CardsExchange> list = List.of(new CardsExchange(), new CardsExchange());
        when(exchangeCardsRepository.findAll()).thenReturn(list);
        assertEquals(list, cardExchangeService.findAll());
    }
    @Test
    void canExchange_sizesBelowAndAboveThreshold() {
        when(cardService.getPlayerCards(10)).thenReturn(List.of());
        assertFalse(cardExchangeService.canExchange(10));

        when(cardService.getPlayerCards(11))
                .thenReturn(List.of(new CardPlayer(), new CardPlayer(), new CardPlayer()));
        assertTrue(cardExchangeService.canExchange(11));
    }


    @Test
    void getArmiesByNextExchange_allSwitchCases() {
        // 0 canjes anteriores
        when(exchangeRepository.findAll()).thenReturn(Collections.emptyList());
        assertEquals(4, cardExchangeService.getArmiesByNextExchange(1));
        // 1 canje anterior
        var c = new Exchange(); c.setPlayerGame(new PlayerGame(){{setId(1);}});
        when(exchangeRepository.findAll()).thenReturn(List.of(c));
        assertEquals(7, cardExchangeService.getArmiesByNextExchange(1));
        // 2 canjes anteriores
        when(exchangeRepository.findAll()).thenReturn(List.of(c, c));
        assertEquals(10, cardExchangeService.getArmiesByNextExchange(1));

        when(exchangeRepository.findAll()).thenReturn(List.of(c, c, c));
        assertEquals(15, cardExchangeService.getArmiesByNextExchange(1));


        when(exchangeRepository.findAll()).thenReturn(Arrays.asList(c,c,c,c,c));
        assertEquals(25, cardExchangeService.getArmiesByNextExchange(1));
    }

    // esCanjeValido(...)
    @Test
    void isValidExchange_rejectsSizeMismatch() {
        assertFalse(cardExchangeService.isValidExchange(List.of(1,2), 1));
    }

    @Test
    void isValidExchange_rejectsMissingCard() {
        when(cardService.getPlayerCards(1)).thenReturn(List.of(t1, t2));
        assertFalse(cardExchangeService.isValidExchange(List.of(1,2,3), 1));
    }

    @Test
    void isValidExchange_rejectsInvalidCombination() {

        t1.getCardCountry().setSymbol(new Symbol(5,""));
        t2.getCardCountry().setSymbol(new Symbol(5,""));
        t3.getCardCountry().setSymbol(new Symbol(6,""));
        when(cardService.getPlayerCards(1)).thenReturn(threeCards);
        assertFalse(cardExchangeService.isValidExchange(List.of(1,2,3), 1));
    }

    @Test
    void isValidExchange_acceptsThreeDistinct() {
        // símbolos [1,2,3]
        when(cardService.getPlayerCards(2)).thenReturn(threeCards);
        assertTrue(cardExchangeService.isValidExchange(List.of(1,2,3), 2));
    }

    @Test
    void isValidExchange_acceptsThreeSame() {
        // símbolos todos iguales
        t1.getCardCountry().setSymbol(new Symbol(9,""));
        when(cardService.getPlayerCards(3)).thenReturn(List.of(t1,t1,t1));
        assertTrue(cardExchangeService.isValidExchange(List.of(1,1,1),3));
    }

    // realizarCanje(...)
    @Test
    void doExchange_insufficientCards_returnsFalse() {
        when(cardService.getPlayerCards(1))
                .thenReturn(List.of(t1, t2)); // menos de 3
        assertFalse(cardExchangeService.doExchange(1, /*partidaId=*/1));
    }

    @Test
    void doExchange_sufficientCallsAllInteractions() {
        // preparar 3 tarjetas válidas para partidaId=1
        when(cardService.getPlayerCards(1)).thenReturn(threeCards);
        // marcarTarjetaComoUsada
        doNothing().when(cardService).markCardAsUsed(anyInt());
        // canjeRepository.save
        Exchange savedExchange = new Exchange();
        savedExchange.setArmyAmount(4);
        when(exchangeRepository.save(any())).thenReturn(savedExchange);
        // historial
        when(registerMessageEvent.exchangeCardsRegister(any(), anyInt()))
                .thenReturn("msg");
        doNothing().when(historyService).registerEvent(any(), anyString());

        boolean ok = cardExchangeService.doExchange(1,1);
        assertTrue(ok);

        // verificar 3 marcas como usadas
        verify(cardService, times(3)).markCardAsUsed(anyInt());
        // verificar guardado de canje
        verify(exchangeRepository).save(argThat(cz -> cz.getArmyAmount()==4));
        // verificar asociación de tarjetas al canje
        verify(exchangeCardsRepository, times(3)).save(any());
        // historial registrado
        verify(historyService).registerEvent(any(), eq("msg"));
    }

    // canjearTarjetas(...)
    @Test
    void exchangeCards_invalidIsValidExchange_returnsFalse() {
        // al no stubear tarjetas, esCanjeValido() dará false
        assertFalse(cardExchangeService.exchangeCards(List.of(1,2,3), 1, 1));
    }

    @Test
    void exchangeCards_throwsWhenPlayerNotFound() {
        // forzar un valid combo
        when(cardService.getPlayerCards(2)).thenReturn(threeCards);
        when(cardService.getCardsByIds(anyList())).thenReturn(threeCards);
        when(playerService.findPlayerGameById(2)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class,
                () -> cardExchangeService.exchangeCards(List.of(1,2,3),2,99),
                "Jugador no encontrado");
    }

    @Test
    void exchangeCards_validUpdatesCounterAndArmies() {
        // preparar combo válido
        when(cardService.getPlayerCards(3)).thenReturn(threeCards);
        when(cardService.getCardsByIds(anyList())).thenReturn(threeCards);
        // preparar jugadorPartida
        PlayerGame jp = new PlayerGame();
        jp.setId(3);
        BasePlayer base = new BasePlayer() {
        }; base.setId(7); base.setAvailableArmies(0);
        jp.setPlayer(base);
        Game p = new Game(); p.setId(50);
        jp.setGame(p);
        Color color = new Color(); color.setId(21);
        jp.setColor(color);
        when(playerService.findPlayerGameById(3)).thenReturn(Optional.of(jp));
        // ejecutar dos canjes seguidos
        boolean first = cardExchangeService.exchangeCards(List.of(1,2,3),3,50);
        assertTrue(first);
        assertEquals(4, base.getAvailableArmies());
        boolean second = cardExchangeService.exchangeCards(List.of(1,2,3),3,50);
        assertTrue(second);
        // ahora debe sumar 7 al anterior 4 = 11
        assertEquals(11, base.getAvailableArmies());
        // verificar que elimine tarjetas y guarde jugador
        verify(cardService, times(2)).deletePlayerCards(threeCards);
        verify(playerService, times(2))
                .savePlayerGame(eq(50), eq(7), eq(21));
    }
}
