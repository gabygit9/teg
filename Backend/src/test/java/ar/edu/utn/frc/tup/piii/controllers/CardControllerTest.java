package ar.edu.utn.frc.tup.piii.controllers;

import ar.edu.utn.frc.tup.piii.dto.ArmyRequestDto;
import ar.edu.utn.frc.tup.piii.dto.PlayerCardDto;
import ar.edu.utn.frc.tup.piii.mappers.CardMapper;
import ar.edu.utn.frc.tup.piii.model.entities.CardPlayer;
import ar.edu.utn.frc.tup.piii.model.entities.CardCountry;
import ar.edu.utn.frc.tup.piii.services.interfaces.CardExchangeService;
import ar.edu.utn.frc.tup.piii.services.interfaces.DeckService;
import ar.edu.utn.frc.tup.piii.services.interfaces.CardService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardControllerTest {

    @Mock private CardService cardService;
    @Mock private DeckService deckService;
    @Mock private CardExchangeService cardExchangeService;
    @InjectMocks private CardController controller;

    @Test
    void initDeck_ok() {
        ResponseEntity<Void> resp = controller.initDeck();
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNull(resp.getBody());
        verify(deckService).initDeck();
    }

    @Test
    void getAvailableCard_ok() {
        CardCountry country = new CardCountry();
        when(cardService.getAvailableCard()).thenReturn(country);

        ResponseEntity<CardCountry> resp = controller.getAvailableCard();
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertSame(country, resp.getBody());
    }

    @Test
    void getAvailableCard_nullBody() {
        when(cardService.getAvailableCard()).thenReturn(null);

        ResponseEntity<CardCountry> resp = controller.getAvailableCard();
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNull(resp.getBody());
    }

    @Test
    void getPlayerCards_ok() {
        List<CardPlayer> entities = List.of(new CardPlayer(), new CardPlayer());
        List<PlayerCardDto> dtos = List.of(new PlayerCardDto(), new PlayerCardDto());

        when(cardService.getPlayerCards(5)).thenReturn(entities);
        try (MockedStatic<CardMapper> ms = mockStatic(CardMapper.class)) {
            ms.when(() -> CardMapper.toDto(entities.get(0))).thenReturn(dtos.get(0));
            ms.when(() -> CardMapper.toDto(entities.get(1))).thenReturn(dtos.get(1));

            ResponseEntity<List<PlayerCardDto>> resp = controller.getPlayerCards(5);
            assertEquals(HttpStatus.OK, resp.getStatusCode());
        }
    }

    @Test
    void getPlayerCards_emptyList() {
        when(cardService.getPlayerCards(7)).thenReturn(List.of());

        ResponseEntity<List<PlayerCardDto>> resp = controller.getPlayerCards(7);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertTrue(resp.getBody().isEmpty());
    }

    @Test
    void requestCard_ok() {
        ResponseEntity<String> resp = controller.requestCard(3);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals("Tarjeta otorgada correctamente (si correspondía).", resp.getBody());
        verify(cardService).askCard(3);
    }

    @Test
    void requestCard_badRequest_onIllegalArgument() {
        doThrow(new IllegalArgumentException("No cumple")).when(cardService).askCard(4);

        ResponseEntity<String> resp = controller.requestCard(4);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("No cumple", resp.getBody());
    }

    @Test
    void requestCard_internalError_onException() {
        doThrow(new RuntimeException()).when(cardService).askCard(5);

        ResponseEntity<String> resp = controller.requestCard(5);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
        assertEquals("Error al solicitar tarjeta.", resp.getBody());
    }

    @Test
    void isValidExchange_trueAndFalse() {
        when(cardExchangeService.isValidExchange(List.of(1,2,3), 9)).thenReturn(true);
        ResponseEntity<Boolean> respTrue = controller.esCanjeValido(List.of(1,2,3), 9);
        assertEquals(HttpStatus.OK, respTrue.getStatusCode());
        assertTrue(Boolean.TRUE.equals(respTrue.getBody()));

        when(cardExchangeService.isValidExchange(List.of(4,5,6), 8)).thenReturn(false);
        ResponseEntity<Boolean> respFalse = controller.esCanjeValido(List.of(4,5,6), 8);
        assertEquals(HttpStatus.OK, respFalse.getStatusCode());
        assertFalse(Boolean.TRUE.equals(respFalse.getBody()));
    }

    @Test
    void exchangeCards_trueAndFalse() {
        when(cardExchangeService.exchangeCards(List.of(7,8), 2, 99)).thenReturn(true);
        ResponseEntity<Boolean> respTrue = controller.exchangeCards(2, List.of(7,8), 99);
        assertEquals(HttpStatus.OK, respTrue.getStatusCode());
        assertTrue(Boolean.TRUE.equals(respTrue.getBody()));

        when(cardExchangeService.exchangeCards(List.of(3), 1, 55)).thenReturn(false);
        ResponseEntity<Boolean> respFalse = controller.exchangeCards(1, List.of(3), 55);
        assertEquals(HttpStatus.OK, respFalse.getStatusCode());
        assertFalse(Boolean.TRUE.equals(respFalse.getBody()));
    }

    @Test
    void assignCard_ok() {
        CardCountry pais = new CardCountry();
        when(cardService.assignCardToPlayer(11, 22)).thenReturn(pais);

        ResponseEntity<CardCountry> resp = controller.assignCard(11, 22);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertSame(pais, resp.getBody());
    }

    @Test
    void markCardAsUsed_ok() {
        ResponseEntity<Void> resp = controller.markCardAsUsed(33);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNull(resp.getBody());
        verify(cardService).markCardAsUsed(33);
    }

    @Test
    void claimArmiesWithCards_ok() {
        ArmyRequestDto dto = new ArmyRequestDto();
        dto.setGameId(5);
        dto.setPlayerGameId(6);
        dto.setCardId(7);

        ResponseEntity<String> resp = controller.claimArmiesWithCards(dto);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNull(resp.getBody());
        verify(cardService).giveArmiesWithCards(5, 6, 7);
    }

    @Test
    void claimArmiesWithCards_badRequest_onIllegalArgument() {
        ArmyRequestDto dto = new ArmyRequestDto();
        dto.setGameId(5);
        dto.setPlayerGameId(6);
        dto.setCardId(7);
        doThrow(new IllegalArgumentException("X"))
                .when(cardService).giveArmiesWithCards(5,6,7);

        ResponseEntity<String> resp = controller.claimArmiesWithCards(dto);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("X", resp.getBody());
    }

    @Test
    void claimArmiesWithCards_internalError_onException() {
        ArmyRequestDto dto = new ArmyRequestDto();
        dto.setGameId(5);
        dto.setPlayerGameId(6);
        dto.setCardId(7);
        doThrow(new RuntimeException())
                .when(cardService).giveArmiesWithCards(5,6,7);

        ResponseEntity<String> resp = controller.claimArmiesWithCards(dto);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
        assertEquals("Error al otorgar ejércitos.", resp.getBody());
    }

    @Test
    void markConquer_ok() {
        ResponseEntity<String> resp = controller.markConquer(44);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals("Conquista marcada manualmente.", resp.getBody());
        verify(cardService).markConquer(44);
    }

    @Test
    void restartDeck_ok() {
        ResponseEntity<String> resp = controller.restartDeck();
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals("Mazo reiniciado", resp.getBody());
        verify(deckService).getCardsInDeck();
    }
}
