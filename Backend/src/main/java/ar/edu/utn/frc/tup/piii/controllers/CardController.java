package ar.edu.utn.frc.tup.piii.controllers;

import ar.edu.utn.frc.tup.piii.dto.ArmyRequestDto;
import ar.edu.utn.frc.tup.piii.dto.PlayerCardDto;
import ar.edu.utn.frc.tup.piii.mappers.CardMapper;
import ar.edu.utn.frc.tup.piii.model.entities.CardPlayer;
import ar.edu.utn.frc.tup.piii.model.entities.CardCountry;
import ar.edu.utn.frc.tup.piii.services.interfaces.CardExchangeService;
import ar.edu.utn.frc.tup.piii.services.interfaces.DeckService;
import ar.edu.utn.frc.tup.piii.services.interfaces.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cards")
public class CardController {
    private final CardService cardService;
    private final DeckService deckService;
    private final CardExchangeService exchangeService;

    /**
     * Inicializa y mezcla el mazo de tarjetas.
     */
    @PostMapping("/init-deck")
    public ResponseEntity<Void> initDeck() {
        deckService.initDeck();
        return ResponseEntity.ok().build();
    }
    /**
     * Devuelve una tarjeta de país aleatoria no asignada.
     * @return TarjetaPais disponible o null si no hay ninguna.
     */
    @GetMapping("/country/available")
    public ResponseEntity<CardCountry> getAvailableCard() {
        return ResponseEntity.ok(cardService.getAvailableCard());
    }

    /**
     * Obtiene todas las tarjetas de un jugador en una partida.
     *
     * @param playerGameId ID del jugador en la partida.
     * @return Lista de tarjetas del jugador.
     */
    @GetMapping("/player/{playerGameId}")
    public ResponseEntity<List<PlayerCardDto>> getPlayerCards(@PathVariable int playerGameId) {
        List<CardPlayer> cards = cardService.getPlayerCards(playerGameId);
        List<PlayerCardDto> dtos = cards.stream()
                .map(CardMapper::toDto)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    /**
     * Solicita una tarjeta para el jugador (si corresponde).
     * Solo se otorga si el jugador ha cumplido su objetivo de conquista.
     */
    @PostMapping("/player/{playerGameId}/request")
    public ResponseEntity<String> requestCard(@PathVariable int playerGameId) {
        try {
            cardService.askCard(playerGameId);
            return ResponseEntity.ok("Tarjeta otorgada correctamente (si correspondía).");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al solicitar tarjeta.");
        }
    }


    /**
     * Canjea una lista de tarjetas para un jugador.
     *
     * @param playerGameId ID del jugador en la partida.
     * @param cardsIds Lista de IDs de tarjetas a canjear.
     * @return true si el canje fue exitoso.
     */
    @PostMapping("/player/{playerGameId}/valid-exchange")
    public ResponseEntity<Boolean> esCanjeValido(
            @RequestParam List<Integer> cardsIds,
            @PathVariable int playerGameId
    ) {
        boolean success = exchangeService.isValidExchange(cardsIds, playerGameId);
        return ResponseEntity.ok(success);
    }

    @PostMapping("/player/{playerGameId}/exchange")
    public ResponseEntity<Boolean> exchangeCards(
            @PathVariable int playerGameId,
            @RequestParam List<Integer> cardsIds,
            @RequestParam int gameId) {

        boolean result = exchangeService.exchangeCards(cardsIds, playerGameId, gameId);
        return ResponseEntity.ok(result);
    }

    /**
     * Asigna una tarjeta a un jugador de una partida.
     * @param cardId ID de la tarjeta.
     * @param playerGameId ID del jugador.
     */
    @PostMapping("/player/{playerGameId}/assign")
    public ResponseEntity<CardCountry> assignCard(@RequestParam int cardId,
                                                  @RequestParam int playerGameId) {
        CardCountry card = cardService.assignCardToPlayer(cardId, playerGameId);
        return ResponseEntity.ok(card);
    }

    /**
     * Marca una tarjeta de jugador como usada luego de canjearla.
     * @param playerCardId ID de la tarjeta del jugador.
     */
    @PutMapping("/mark-used/{playerCardId}")
    public ResponseEntity<Void> markCardAsUsed(@PathVariable int playerCardId) {
        cardService.markCardAsUsed(playerCardId);
        return ResponseEntity.ok().build();
    }

    /**
     * Reclama ejércitos por las tarjetas del jugador que coincidan con países conquistados.
     */
    @PostMapping("/player/ask-armies")
    public ResponseEntity<String> claimArmiesWithCards(@RequestBody ArmyRequestDto request) {
        try {
            cardService.giveArmiesWithCards(
                    request.getGameId(),
                    request.getPlayerGameId(),
                    request.getCardId()
            );
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error al otorgar ejércitos.");
        }
    }

    //solo para probar.. eliminar despues
    @PostMapping("/player/{playerGameId}/mark-conquer")
    public ResponseEntity<String> markConquer(@PathVariable int playerGameId) {
        cardService.markConquer(playerGameId);
        return ResponseEntity.ok("Conquista marcada manualmente.");
    }
    //solo para probar.. eliminar despues
    @PostMapping("/deck/restart")
    public ResponseEntity<String> restartDeck() {
        deckService.getCardsInDeck(); // método que reemplace las tarjetas si ya no hay
        return ResponseEntity.ok("Mazo reiniciado");
    }


}
