package ar.edu.utn.frc.tup.piii.services.impl;

import ar.edu.utn.frc.tup.piii.model.entities.*;
import ar.edu.utn.frc.tup.piii.model.enums.StateGameEnum;
import ar.edu.utn.frc.tup.piii.model.enums.TurnPhase;
import ar.edu.utn.frc.tup.piii.model.repository.TurnRepository;
import ar.edu.utn.frc.tup.piii.services.interfaces.*;
import ar.edu.utn.frc.tup.piii.util.RegisterMessageEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;


/**
 * Implementación del servicio de turnos.
 * Controla el flujo de turnos dentro de una partida, administrando fases y orden de jugadores.
 */
@Service
public class TurnServiceImpl implements TurnService {

    private final TurnRepository turnRepository;
    private final PlayerService playerService;
    private final GameService gameService;
    private final CountryGameService countryGameService;
    private final HistoryService historyService;
    private final CardService cardService;
    private GameStateService gameStateService;
    private ObjectiveService objectiveService;

    @Autowired
    public TurnServiceImpl(
            TurnRepository turnRepository,
            @Lazy PlayerService playerService,
            @Lazy GameService gameService,
            CountryGameService countryGameService,
            HistoryService historyService,
            CardService cardService,
            GameStateService gameStateService,
            @Lazy ObjectiveService objectiveService) {
        this.turnRepository = turnRepository;
        this.playerService = playerService;
        this.gameService = gameService;
        this.countryGameService = countryGameService;
        this.historyService = historyService;
        this.cardService = cardService;
        this.gameStateService = gameStateService;
        this.objectiveService = objectiveService;
    }

    @Override
    public boolean save(Turn turn){
        Turn turnSave = turnRepository.save(turn);
        return turnSave.getId() > 0;
    }

    @Override
    public Turn findById(int id) {
        return turnRepository.findById(id).orElse(null);
    }

    @Override
    public List<Turn> findAll() {
        return turnRepository.findAll();
    }

    @Override
    public void startTurn(PlayerGame playerGame, Game game) {
        playerGame.setTurn(true);
        Turn turn = new Turn();
        turn.setPlayerGame(playerGame);
        turn.setInitialStartDate(LocalDateTime.now());
        turn.setCurrentPhase(TurnPhase.INCORPORATION);
        turn.setGame(game);
        turn.setMaxDuration(180);
        turn.setAvailableArmies(playerGame.getPlayer().getAvailableArmies());
        turn.setFinished(false);
        save(turn);

        // Registrar en historial
        String message = RegisterMessageEvent.startTurn(playerGame.getPlayer());
        historyService.registerEvent(game, message);

    }

    @Override
    public void movePhase(Turn turn) {
        switch (turn.getCurrentPhase()) {
            case INCORPORATION -> turn.setCurrentPhase(TurnPhase.ATTACK);
            case ATTACK -> turn.setCurrentPhase(TurnPhase.REGROUPING);
            case REGROUPING -> {
                return; // No hay siguiente fase
            }
        }
        save(turn);

        // Registrar en historial
        String message = RegisterMessageEvent.changePhase(turn.getPlayerGame().getPlayer(), turn.getCurrentPhase());
        historyService.registerEvent(turn.getGame(), message);

    }

    @Override
    public List<String> getAvailableActions(Turn turn) {

        List<String> actions = new ArrayList<>();
        StateGameEnum state = StateGameEnum.valueOf(turn.getGame().getStates().getDescription().toUpperCase());

        // Evaluamos según el state de la partida
        switch (state) {
            case FIRST_ROUND, SECOND_ROUND -> actions.add("putArmies");

            case HOSTILITIES -> {
                TurnPhase phase = turn.getCurrentPhase();

                if (phase == null) break;

                switch (phase) {
                    case INCORPORATION -> actions.add("putArmies");

                    case ATTACK -> {
                        actions.add("attack");
                        actions.add("askCard");
                        actions.add("requestArmies");
                        actions.add("exchange");
                    }

                    case REGROUPING -> {
                        actions.add("moveArmies");
                        actions.add("askCard");
                        actions.add("requestArmies");
                    }
                }
            }
            default -> {
                // No hay actions disponibles
            }
        }
        /*
        try {
            state = EstadoPartida.valueOf(turno.getPartida().getEstado().getDescripcion().toUpperCase());
            for (String accion : actions) {
                System.out.println(accion);
            }
        } catch (Exception e) {
            System.out.println("Error al parsear EstadoPartida: " + e.getMessage());
            return actions;
        }*/


        return actions;
    }

    @Override
    public void finishTurn(Turn turn) {
        PlayerGame jp = turn.getPlayerGame();
        jp.setTurn(false);
        playerService.update(jp.getPlayer());
        turn.setFinished(true);
        save(turn);
    }

    @Override
    public void finishTurnRound(int id, Turn turn) {
        PlayerGame playerGame = playerService.findPlayerGameById(id)
                .orElseThrow(() -> {
                    return new IllegalArgumentException("JugadorPartida no encontrado");
                });

        Game game = playerGame.getGame();

        List<PlayerGame> players = playerService.findByGameId(game.getId());

        // Lista de players activos y
        List<PlayerGame> playersSorted = players.stream()
                .filter(PlayerGame::isActive)
                .sorted(Comparator.comparingInt(PlayerGame::getOrderTurn))
                .toList();

        // Buscar next playerGame activo
        Optional<PlayerGame> next = playersSorted.stream()
                .filter(j -> j.getOrderTurn() > playerGame.getOrderTurn())
                .findFirst();

        if (next.isEmpty()) {
            next = playersSorted.stream().findFirst();
        }

        if (next.isEmpty()) return;
        //desactivar
        playerGame.setTurn(false);
        playerService.update(playerGame.getPlayer());

        //activar next playerGame
        PlayerGame prox = next.get();
        prox.setTurn(true);
        playerService.update(prox.getPlayer());

        startTurn(prox, prox.getGame());

        int quantityTurns = turnRepository.findByGame_Id(turn.getGame().getId()).toArray().length;
        int quantityPlayers = playersSorted.toArray().length;

        //asignar refuerzos despues de la 3ra ronda
        String descriptionsState = game.getStates().getDescription().toUpperCase();
        if ("HOSTILITIES".equals(descriptionsState) && quantityTurns >= quantityPlayers * 3) {
            int countries = prox.getCountries().size();
            int reinforcement = (int) Math.floor(countries / 2.0);
            int currentArmy = prox.getPlayer().getAvailableArmies();
            prox.getPlayer().setAvailableArmies(reinforcement + currentArmy);
            playerService.update(prox.getPlayer());
        }

        if (playerGame == playersSorted.get(playersSorted.size()-1) && !"HOSTILITIES".equals(descriptionsState)) {
            gameService.moveState(game.getId());
        }

    }

    @Override
    public void putArmy(int gameId, int countryId, int quantity) {

        PlayerGame player = playerService.findPlayerGameById(gameId)
                .orElseThrow(() -> {
                    return new IllegalArgumentException("Jugador no encontrado");
                });

        if (player.getPlayer() == null) {
            throw new IllegalStateException("El JugadorBase no está inicializado");
        }

        if (player.getPlayer().getAvailableArmies() < quantity) {
            throw new IllegalArgumentException("No tiene suficientes ejércitos disponibles");
        }

        CountryGameId id = new CountryGameId(countryId, player.getGame().getId());
        CountryGame country = countryGameService.findById(id.getCountryId(), id.getGameId());

        if (country.getPlayerGame() == null || !(country.getPlayerGame().getId() == (player.getId()))) {
            throw new IllegalArgumentException("El país no pertenece al player");
        }



        country.setAmountArmies(country.getAmountArmies() + quantity);
        player.getPlayer().setAvailableArmies(player.getPlayer().getAvailableArmies() - quantity);

        countryGameService.save(country);
        playerService.update(player.getPlayer());

        // Registrar en historial
        String message = RegisterMessageEvent.putArmy(
                player.getPlayer(),
                country.getCountry(),
                quantity
        );
        historyService.registerEvent(player.getGame(), message);
    }

    @Override
    public Turn getPlayerGameId(int playerGameId) {
        return turnRepository.findByPlayerGame_IdAndFinishedFalse(playerGameId);
    }

    @Override
    public void moveArmies(CountryGameId idOrigin, CountryGameId idDestination, int troopsToMove) {
        CountryGame origin = countryGameService.findById(idOrigin.getCountryId(), idOrigin.getGameId());
        CountryGame destination = countryGameService.findById(idDestination.getCountryId(), idDestination.getGameId());

        // si no pertenecen al mismo jugador
        if (!origin.getPlayerGame().equals(destination.getPlayerGame())) {
            throw new IllegalArgumentException("Ambos países deben pertenecer al mismo jugador");
        }
        //si la cantidad es mayor a los ejércitos disponibles para mover
        if (troopsToMove > (origin.getAmountArmies() - 1)) {
            throw new IllegalArgumentException("No se puede dejar el país sin al menos un ejército");
        }

        origin.setAmountArmies(origin.getAmountArmies() - troopsToMove);
        destination.setAmountArmies(destination.getAmountArmies() + troopsToMove);

        countryGameService.save(origin);
        countryGameService.save(destination);
    }

    @Override
    public void startFirstTurnOfTheGame(Game game) {
        List<PlayerGame> all = playerService.findByGameId(game.getId());
        for(PlayerGame playerGame : all) {
            playerGame.getPlayer().setAvailableArmies(0);
        }


        System.out.println("=== DEBUG TURNOS ===");
        all.forEach(j -> System.out.println("Jugador: " + j.getPlayer().getName() +
                ", ordenTurno: " + j.getOrderTurn() +
                ", esTurno: " + j.isTurn() +
                ", ejercitos: " + j.getPlayer().getAvailableArmies() +
                ", esBot: " + (j.getPlayer() instanceof BotPlayer)));

        PlayerGame jp = all
                .stream()
                .filter(j -> j.getOrderTurn() == 1)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("no se encontró el jugador con el primer turno"));

        startTurn(jp, game);
        // Si el primer jugador es bot, ejecutar su turno automáticamente
        /*if (jp.getJugador() instanceof JugadorBot) {
            System.out.println("El primer jugador es un bot, ejecutando turno automáticamente...");
            jugadorService.ejecutarTurnoBot(jp, partida);
        }*/

        System.out.println("Primer jugador encontrado: " + jp.getPlayer().getName());
    }



    @Override
    public int getPlayerInTurn(int gameId) {
        Turn turn = turnRepository.findByGame_IdAndFinishedFalse(gameId)
                .orElseThrow(()-> new NoSuchElementException("No se pudo encontrar el turno con la partida especificada."));
        return turn.getPlayerGame().getId();
    }

   }