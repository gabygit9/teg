package ar.edu.utn.frc.tup.piii.services.impl;

import ar.edu.utn.frc.tup.piii.model.entities.CardCountry;
import ar.edu.utn.frc.tup.piii.model.repository.CardPlayerRepository;
import ar.edu.utn.frc.tup.piii.model.repository.CardCountryRepository;
import ar.edu.utn.frc.tup.piii.services.interfaces.DeckService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
//@RequiredArgsConstructor
public class DeckServiceImpl implements DeckService {

    public DeckServiceImpl(CardCountryRepository cardCountryRepository, CardPlayerRepository cardPlayerRepository) {
        this.cardCountryRepository = cardCountryRepository;
        this.cardPlayerRepository = cardPlayerRepository;
    }

    private final CardCountryRepository cardCountryRepository;
    private final CardPlayerRepository cardPlayerRepository;

    private List<CardCountry> deckGlobal;
    private int currentIndexGlobal = 0;

    private final Map<Integer, List<CardCountry>> deckByGame = new ConcurrentHashMap<>();
    private final Map<Integer, Integer> indexPerGame = new ConcurrentHashMap<>();

    /**
     * Inicialización automática del mazo global al arrancar el servicio.
     */
    @PostConstruct
    public void initDeckAtStart() {
        initDeck();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initDeck() {
        List<CardCountry> allCards = cardCountryRepository.findAll();

        if (allCards.isEmpty()) {
            deckGlobal = new ArrayList<>();
            return;
        }

        deckGlobal = new ArrayList<>(allCards);

        Collections.shuffle(deckGlobal);
        currentIndexGlobal = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initDeckToGame(int gameId) {
        List<CardCountry> allCards = cardCountryRepository.findAll();

        if (allCards.isEmpty()) {
            deckByGame.put(gameId, new ArrayList<>());
            return;
        }

        List<CardCountry> deckGame = new ArrayList<>(allCards);
        Collections.shuffle(deckGame);

        deckByGame.put(gameId, deckGame);
        indexPerGame.put(gameId, 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CardCountry getAvailableCards() {
        if (deckGlobal == null || deckGlobal.isEmpty()) {
            return null;
        }

        List<CardCountry> availableCards = getAvailableCardsIntern(deckGlobal);

        if (availableCards.isEmpty()) {
            restartDeck();
            availableCards = getAvailableCardsIntern(deckGlobal);

            if (availableCards.isEmpty()) {
                return null;
            }
        }

        return availableCards.get(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CardCountry getAvailableCardsToGame(int gameId) {
        List<CardCountry> deckGame = deckByGame.get(gameId);

        if (deckGame == null || deckGame.isEmpty()) {
            return null;
        }

        List<CardCountry> availableCards = getAvailableCarsInternPerGame(deckGame, gameId);

        if (availableCards.isEmpty()) {
            restartDeckToGame(gameId);
            availableCards = getAvailableCarsInternPerGame(deckGame, gameId);

            if (availableCards.isEmpty()) {
                return null;
            }
        }

        return availableCards.get(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shuttleDeck() {
        if (deckGlobal != null && !deckGlobal.isEmpty()) {
            Collections.shuffle(deckGlobal);
            currentIndexGlobal = 0;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int countAvailableCards() {
        if (deckGlobal == null) {
            return 0;
        }
        return getAvailableCardsIntern(deckGlobal).size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int countAvailableCardsToGame(int gameId) {
        List<CardCountry> deckGame = deckByGame.get(gameId);
        if (deckGame == null) {
            return 0;
        }
        return getAvailableCarsInternPerGame(deckGame, gameId).size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void restartDeck() {
        initDeck();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void restartDeckToGame(int gameId) {
        initDeckToGame(gameId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmptyDeck() {
        return countAvailableCards() == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmptyDeckToGame(int gameId) {
        return countAvailableCardsToGame(gameId) == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CardCountry> getCardsInDeck() {
        if (deckGlobal == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(getAvailableCardsIntern(deckGlobal));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void returnCardToDeck(CardCountry cardCountry) {
        if (cardCountry != null) {
            cardPlayerRepository.findAll().stream()
                    .filter(tj -> tj.getCardCountry().getId() == cardCountry.getId())
                    .findFirst()
                    .ifPresent(playerCard -> {
                        playerCard.setUsed(false);
                        cardPlayerRepository.delete(playerCard);
                    });
        }
    }


    /**
     * Obtiene las tarjetas que no están asignadas a ningún jugador.
     */
    private List<CardCountry> getAvailableCardsIntern(List<CardCountry> deck) {
        if (deck == null || deck.isEmpty()) {
            return new ArrayList<>();
        }

        Set<Integer> assignedCards = cardPlayerRepository.findAll().stream()
                .map(tj -> tj.getCardCountry().getId())
                .collect(Collectors.toSet());

        return deck.stream()
                .filter(card -> !assignedCards.contains(card.getId()))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene las tarjetas disponibles para una partida específica.
     * Filtra las tarjetas que no están asignadas a jugadores en esa partida.
     */
    private List<CardCountry> getAvailableCarsInternPerGame(List<CardCountry> deck, int gameId) {
        if (deck == null || deck.isEmpty()) {
            return new ArrayList<>();
        }

        Set<Integer> assignedCardsInGame = cardPlayerRepository.findAll().stream()
                .filter(tj -> tj.getPlayerGame().getGame().getId() == gameId)
                .map(tj -> tj.getCardCountry().getId())
                .collect(Collectors.toSet());

        return deck.stream()
                .filter(card -> !assignedCardsInGame.contains(card.getId()))
                .collect(Collectors.toList());
    }
}