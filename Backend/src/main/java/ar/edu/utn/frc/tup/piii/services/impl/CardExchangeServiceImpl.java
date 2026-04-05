package ar.edu.utn.frc.tup.piii.services.impl;

import ar.edu.utn.frc.tup.piii.model.entities.*;
import ar.edu.utn.frc.tup.piii.model.repository.ExchangeRepository;
import ar.edu.utn.frc.tup.piii.model.repository.ExchangeCardsRepository;
import ar.edu.utn.frc.tup.piii.services.interfaces.CardExchangeService;
import ar.edu.utn.frc.tup.piii.services.interfaces.HistoryService;
import ar.edu.utn.frc.tup.piii.services.interfaces.PlayerService;
import ar.edu.utn.frc.tup.piii.services.interfaces.CardService;
import ar.edu.utn.frc.tup.piii.util.RegisterMessageEvent;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Implementación del servicio que gestiona el proceso de canje de tarjetas por ejércitos.
 *
 * @author Ismael Ceballos
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class CardExchangeServiceImpl implements CardExchangeService {

    private final ExchangeCardsRepository exchangeCardsRepo;
    private final CardService cardService;
    private final ExchangeRepository exchangeRepository;
    private final HistoryService historyService;
    private final RegisterMessageEvent registerMessageEvent;
    @Lazy
    private final PlayerService playerService;

    private final Map<Integer, Integer> countExchangesPlayer = new HashMap<>();


    @Override
    public boolean save(CardsExchange user){
        CardsExchange CTSave = exchangeCardsRepo.save(user);
        return CTSave.getId().getExchangeId() != 0 && CTSave.getId().getPlayerCardId() != 0;
    }

    @Override
    public CardsExchange findById(int id) {
        return exchangeCardsRepo.findById(id).orElse(null);
    }

    @Override
    public List<CardsExchange> findAll() {
        return exchangeCardsRepo.findAll();
    }

    /**
     * Realiza un canje de tarjetas por ejércitos.
     *
     * @param playerGameId ID del jugador en la partida.
     * @author Mariano
     * @version 1.0
     */
    @Override
    public boolean doExchange(int playerGameId, int gameId) {
        // Obtener tarjetas disponibles del jugador
        List<CardPlayer> availableCards = cardService.getPlayerCards(playerGameId).stream()
                .filter(t -> !t.isUsed() && t.getPlayerGame().getId() == gameId)
                .limit(3)
                .toList();

        // No tiene suficientes tarjetas para canjear (3)
        if (availableCards.size() < 3) {
            return false;
        }

        // Marca las tarjetas como usadas
        for (CardPlayer card : availableCards) {
            cardService.markCardAsUsed(card.getId());
        }

        // Crea y guarda el canje
        Exchange exchange = new Exchange();
        exchange.setPlayerGame(availableCards.get(0).getPlayerGame());
        exchange.setDateTime(LocalDateTime.now());
        exchange.setArmyAmount(getArmiesByNextExchange(playerGameId));
        exchange = exchangeRepository.save(exchange);

        // Asocia las tarjetas al canje
        for (CardPlayer card : availableCards) {
            CardsExchange cardsExchange = new CardsExchange();
            cardsExchange.setExchange(exchange);
            cardsExchange.setCardPlayer(card);
            exchangeCardsRepo.save(cardsExchange);
        }

        // registro en historial
        Game game = availableCards.get(0).getPlayerGame().getGame();
        String message = registerMessageEvent.exchangeCardsRegister(
                availableCards.get(0).getPlayerGame(), exchange.getArmyAmount());
        historyService.registerEvent(game, message);

        return true;
    }



    /**
     * Verifica si el jugador puede realizar un canje de tarjetas.
     *
     * @param playerGameId ID del jugador en la partida.
     * @return true si tiene una combinación válida, false si no.
     *
     * @author Mariano
     * @version 1.0
     */
    @Override
    public boolean canExchange(int playerGameId) {
        List<CardPlayer> availableCards = cardService.getPlayerCards(playerGameId);
        return availableCards.size() >= 3;
    }

    /**
     * Retorna la cantidad de ejércitos por el próximo canje disponible.
     * 1º canje . . . . . . . . . . . . . . . . . . . . . . . . . . . . 4 ejércitos
     * 2º canje . . . . . . . . . . . . . . . . . . . . . . . . . . . . 7 ejércitos
     * 3º canje . . . . . . . . . . . . . . . . . . . . . . . . . . . . 10 ejércitos
     * De aquí en adelante se aumentan 5 ejércitos por vez:
     * 4º canje . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 15 ejércitos
     * 5º canje . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 20 ejércitos
     * Y así sucesivamente.
     *
     *
     * @param playerGameId ID del jugador en la partida.
     * @return cantidad de ejércitos.
     *
     * @author Mariano
     * @version 1.0
     */
    @Override
    public int getArmiesByNextExchange(int playerGameId) {
        //Cuenta los canjes anteriores del jugador
        long quantityExchanges = exchangeRepository.findAll().stream()
                .filter(c -> c.getPlayerGame().getId() == playerGameId).count();
        return switch ((int) quantityExchanges) {
            case 0 -> 4;
            case 1 -> 7;
            case 2 -> 10;
            default -> 10 + (((int) quantityExchanges - 2) * 5);
        };
    }


    @Override
    public boolean isValidExchange(List<Integer> cardsIds, int playerGameId) {
        // Obtener las tarjetas que no uso el jugador
        List<CardPlayer> playerCards = cardService.getPlayerCards(playerGameId);

        // Validar que el jugador tenga todas las tarjetas que selecciono
        for (Integer cardId : cardsIds) {
            boolean hasCard = playerCards.stream()
                    .anyMatch(t -> t.getId() == cardId);
            if (!hasCard) {
                return false;
            }
        }

        // Que sean 3
        if (cardsIds.size() != 3) {
            return false;
        }

        // Obtener los símbolos de las tarjetas que selecciono
        List<Symbol> symbols = cardsIds.stream()
                .map(id -> playerCards.stream().filter(t -> t.getId() == id).findFirst().orElse(null))
                .filter(Objects::nonNull)
                .map(t -> t.getCardCountry().getSymbol())
                .toList();

        if (symbols.size() != 3) return false;

        // Validar la combinación de símbolos
        return isValidCombination(symbols);
    }

    private boolean isValidCombination(List<Symbol> symbols) {
        // Iguales
        if (symbols.get(0).getId() == symbols.get(1).getId() &&
                symbols.get(1).getId() == symbols.get(2).getId()) {
            return true;
        }

        // Distintas
        if (symbols.get(0).getId() != symbols.get(1).getId() &&
                symbols.get(0).getId() != symbols.get(2).getId() &&
                symbols.get(1).getId() != symbols.get(2).getId()) {
            return true;
        }
        return false;
    }

    @Transactional
    @Override
    public boolean exchangeCards(List<Integer> cardsIds, int playerGameId, int gameId) {
        if (!isValidExchange(cardsIds, playerGameId)) {
            return false;
        }

        // Obtener las tarjetas seleccionadas por ID
        List<CardPlayer> cardsToRemove = cardService.getCardsByIds(cardsIds);

        // Eliminar del mazo del playerGame
        cardService.deletePlayerCards(cardsToRemove);

        int quantityExchanges = countExchangesPlayer.getOrDefault(playerGameId, 0);
        int armiesToWin = 4 + quantityExchanges * 3;

        // Obtener playerGame y sumar ejércitos
        PlayerGame playerGame = playerService.findPlayerGameById(playerGameId)
                .orElseThrow(() -> new RuntimeException("Jugador no encontrado"));

        playerGame.getPlayer().setAvailableArmies(playerGame.getPlayer().getAvailableArmies() + armiesToWin);

        countExchangesPlayer.put(playerGameId, quantityExchanges + 1);

        playerService.savePlayerGame(playerGame.getGame().getId(), playerGame.getPlayer().getId(), playerGame.getColor().getId());

        return true;
    }

}
