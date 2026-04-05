package ar.edu.utn.frc.tup.piii.services.impl;

import ar.edu.utn.frc.tup.piii.model.entities.*;
import ar.edu.utn.frc.tup.piii.model.repository.CardPlayerRepository;
import ar.edu.utn.frc.tup.piii.model.repository.CardCountryRepository;
import ar.edu.utn.frc.tup.piii.services.interfaces.*;
import ar.edu.utn.frc.tup.piii.util.RegisterMessageEvent;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementación del servicio de tarjetas.
 * Gestiona el reparto, uso y validación de tarjetas dentro de una partida.
 */
@Service
public class CardServiceImpl implements CardService {

    private final CardCountryRepository cardCountryRepository;
    private final CardPlayerRepository cardPlayerRepository;
    private final PlayerService playerService;
    private final CardExchangeService cardExchangeService;
    private final CountryGameService countryGameService;
    private final DeckService deckService;
    private final HistoryService historyService;

    @Autowired
    public CardServiceImpl(
            CardCountryRepository cardCountryRepository,
            CardPlayerRepository cardPlayerRepository,
            @Lazy PlayerService playerService,
            @Lazy CardExchangeService cardExchangeService,
            CountryGameService countryGameService, DeckService deckService, HistoryService historyService) {

        this.cardCountryRepository = cardCountryRepository;
        this.cardPlayerRepository = cardPlayerRepository;
        this.playerService = playerService;
        this.cardExchangeService = cardExchangeService;
        this.countryGameService = countryGameService;
        this.deckService = deckService;
        this.historyService = historyService;
    }

    private final Map<Integer, Boolean> conquestCountryByPlayer = new HashMap<>();

    @Override
    public boolean updateCountry(CardCountry card) {
        if (cardCountryRepository.existsById(card.getId())) {
            cardCountryRepository.save(card);
            return true;
        }
        return false;
    }

    @Override
    public CardCountry findByIdCountry(int id) {
        return cardCountryRepository.findById(id).orElse(null);
    }

    @Override
    public List<CardCountry> findAllCountry() {
        return cardCountryRepository.findAll();
    }

    @Override
    public boolean savePlayer(CardPlayer card){
        CardPlayer playerSaved = cardPlayerRepository.save(card);
        return playerSaved.getId() > 0;
    }

    @Override
    public boolean updatePlayer(CardPlayer card) {
        if (cardPlayerRepository.existsById(card.getId())) {
            cardPlayerRepository.save(card);
            return true;
        }
        return false;
    }

    @Override
    public CardPlayer findByIdPlayer(int id) {
        return cardPlayerRepository.findById(id).orElse(null);
    }

    @Override
    public List<CardPlayer> findAllPlayer() {
        return cardPlayerRepository.findAll();
    }

    //#endregion

    @Override
    public CardCountry getAvailableCard() {
        List<Integer> cardsAlreadyAssigned = cardPlayerRepository.findAll().stream()
                .map(tj -> tj.getCardCountry().getId())
                .toList();
        System.out.println("🃏 Tarjetas disponibles en mazo: " + deckService.getCardsInDeck().size());


        return cardCountryRepository.findAll().stream()
                .filter(tp -> !cardsAlreadyAssigned.contains(tp.getId()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public CardCountry assignCardToPlayer(int cardId, int playerGameId) {
        CardCountry cardCountry = cardCountryRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Tarjeta no encontrada"));

        // Obtener el jugadorPartida real desde la base de datos
        PlayerGame playerGame = playerService.findPlayerGameById(playerGameId)
                .orElseThrow(() -> new IllegalArgumentException("JugadorPartida no encontrado"));

        // Crear y asignar la cardCountry al jugador
        CardPlayer cardPlayer = new CardPlayer();
        cardPlayer.setCardCountry(cardCountry);
        cardPlayer.setPlayerGame(playerGame);
        cardPlayer.setUsed(false);

        cardPlayerRepository.save(cardPlayer);

        // registro en historial
        String message = RegisterMessageEvent.receiveCard(playerGame.getPlayer(), cardCountry.getCountry().getName());
        historyService.registerEvent(playerGame.getGame(), message);


        return cardCountry;

    }

    @Override
    public void markCardAsUsed(int playerCardId) {
        CardPlayer card = cardPlayerRepository.findById(playerCardId).orElse(null);
        if (card != null) {
            card.setUsed(true);
            cardPlayerRepository.save(card);
        }
    }

    @Override
    public List<CardPlayer> getPlayerCards(int playerGameId) {
        return cardPlayerRepository.findByPlayerGame_Id(playerGameId);
    }

    @Override
    public boolean isValidExchange(List<Integer> cardsIds, int playerGameId) {
        return cardExchangeService.isValidExchange(cardsIds, playerGameId);
    }

    @Override
    public void askCard(int playerId) {

        playerService.findPlayerGameById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Jugador no encontrado"));

        if (!conqueredCountry(playerId)) {
            System.out.println("Jugador no conquistò paìs. No se asigna card.");
            return;
        }

        CardCountry card = getAvailableCard();

        System.out.println("Tarjeta obtenida: " + (card != null ? card.getCountry().getName() : "null"));

        if (card != null) {
            assignCardToPlayer(card.getId(), playerId);
        }
        cleanState(playerId);
    }

    @Override
    public void doExchange(int playerId, int gameId) {
        cardExchangeService.doExchange(playerId, gameId);
    }

    @Override
    public void saveAll(List<CardPlayer> cards) {
        cardPlayerRepository.saveAll(cards);
    }

    @Override
    @Transactional
    public void giveArmiesWithCards(int gameId, int playerGameId, int cardId) {
        Optional<PlayerGame> playerGame = playerService.findPlayerGameById(playerGameId);
        CardPlayer cardPlayer = cardPlayerRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Tarjeta no encontrada"));

        if (cardPlayer.isUsed()) {
            throw new IllegalArgumentException("Esta cardPlayer ya fue usada.");
        }

        String nameCountryCard = cardPlayer.getCardCountry().getCountry().getName();

        List<CountryGame> countryGames = countryGameService.findByGameAndPlayerGame(gameId, playerGameId);
        boolean hasCountry = countryGames.stream()
                .anyMatch(p -> p.getCountry().getName().equalsIgnoreCase(nameCountryCard));

        if (!hasCountry) {
            throw new IllegalArgumentException(nameCountryCard);
        }

        cardPlayer.setUsed(true);
        cardPlayerRepository.save(cardPlayer);

        BasePlayer basePlayer = playerGame.get().getPlayer();
        basePlayer.setAvailableArmies(basePlayer.getAvailableArmies() + 2);
        playerService.saveBasePlayer(basePlayer);

        // registro en historial
        String message = RegisterMessageEvent.giveArmiesPerCard(basePlayer, nameCountryCard);
        historyService.registerEvent(playerGame.get().getGame(), message);
    }

    @Override
    public List<CardPlayer> getCardsByIds(List<Integer> ids) {
        return cardPlayerRepository.findAllById(ids);
    }

    @Transactional
    @Override
    public void deletePlayerCards(List<CardPlayer> cards) {
        cardPlayerRepository.deleteAll(cards);
    }


    public void markConquer(int playerGameId) {
        conquestCountryByPlayer.put(playerGameId, true);
        System.out.println("conquista marcada para jugadorId: " + playerGameId);
    }

    public boolean conqueredCountry(int playerGameId) {
        boolean result = conquestCountryByPlayer.getOrDefault(playerGameId, false);
        System.out.println("consulta conquista jugadorId " + playerGameId + ": " + result);
        return result;
    }

    public void cleanState(int playerGameId) {
        conquestCountryByPlayer.remove(playerGameId);
    }
}