package ar.edu.utn.frc.tup.piii.services.impl;

import ar.edu.utn.frc.tup.piii.dto.*;
import ar.edu.utn.frc.tup.piii.model.entities.*;
import ar.edu.utn.frc.tup.piii.model.enums.StateGameEnum;
import ar.edu.utn.frc.tup.piii.model.repository.PlayerGameRepository;
import ar.edu.utn.frc.tup.piii.model.repository.GameRepository;
import ar.edu.utn.frc.tup.piii.services.interfaces.*;
import ar.edu.utn.frc.tup.piii.state.GameContext;
import ar.edu.utn.frc.tup.piii.util.AnalizeObjective;
import ar.edu.utn.frc.tup.piii.util.ProcessedObjective;
import ar.edu.utn.frc.tup.piii.util.ObjectiveType;
import ar.edu.utn.frc.tup.piii.util.RegisterMessageEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Implementación del servicio que gestiona el ciclo de vida y reglas de una partida.
 * Contiene la lógica central de control del flujo del juego.
 */
@Service
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;

    private final CountryGameService countryGameService;
    private final GameStateService gameStateService;
    private final PlayerService playerService;
    private final PlayerGameRepository playerGameRepository;
    private final ObjectiveService objectiveService;
    private final CardService cardService;
    private final RegisterMessageEvent registerMessageEvent;
    private final HistoryService historyService;

    private final TurnService turnService;


    @Override
    public boolean save(Game game){
        Game gameSave = gameRepository.save(game);
        return gameSave.getId() > 0;
    }

    @Override
    public Game findById(int id) {
        return gameRepository.findById(id).orElse(null);
    }

    @Override
    public List<Game> findAll() {
        List<StateGameEntity> eActives = new ArrayList<>();
        eActives.add(gameStateService.findByDescription("paused"));
        eActives.add(gameStateService.findByDescription("in course"));
        eActives.add(gameStateService.findByDescription("preparation"));
        return gameRepository.findByStatesIn(eActives);
    }

    public Game dtoToEntity(GameDTO game) {
        StateGameEntity estado = gameStateService.findById(game.getStateId());
        Objective objective = objectiveService.findById(game.getCommonObjectiveId());

        return new Game(0,
                game.getDateTime(),
                estado,
                game.getCommunicationType(),
                objective
        );
    }

    @Override
    public GameDTO entityToDto(Game game) {
        return new GameDTO(
                game.getId(),
                0,
                game.getObjectiveCommon(),
                0,
                game.getStates(),
                game.getCommunicationType(),
                game.getStartDate()
        );
    }

    @Override
    public List<PlayerGame> playersOfAGame(int gameId) {
        return List.of();
    }

    @Override
    public Game startGame(Game game) {
        game.setStartDate(LocalDateTime.now());
        StateGameEntity ep = gameStateService.findByDescription("preparation");
        game.setStates(ep);
        save(game);

        // registro en historial
        String message = registerMessageEvent.startGameRegistry(game);
        historyService.registerEvent(game, message);

        return game;
    }

    public boolean continueGame(int gameId) {
        boolean ret = false;
        Game game = findById(gameId);
        StateGameEntity ep = gameStateService.findByDescription("paused");
        if (game.getStates() == ep) {
            //game.getStates(gameStateService.findByDescription("in course"));
            ret = save(game);


            // registro en historial
            String message = registerMessageEvent.continueGameRegistry(game);
            historyService.registerEvent(game, message);

        }
        return ret;
    }

    @Override
    public boolean endGame(int gameId) {
        Game game = findById(gameId);
        StateGameEntity ep = gameStateService.findByDescription("FINISHED");
        if (!game.getStates().getDescription().equalsIgnoreCase("FINISHED")) {
            game.setStates(ep);

            // registro en historial
            String message = registerMessageEvent.finishGameRegistry(game);
            historyService.registerEvent(game, message);

            return save(game);


        }
        return false;
    }

    public void initHostilities(int gameId) {
        Game game = findById(gameId);
        new GameContext(game, this, countryGameService, cardService, playerService, gameStateService, objectiveService).moveState(game);
        save(game);

        // registro en historial
        String message = registerMessageEvent.startHostilitiesGameRegistry(gameId);
        historyService.registerEvent(game, message);

    }

    @Override
    public List<Integer> throwDice(int attackerId, int defenderId, int maxDice) {
        return List.of();
    }

    @Override
    public void conquerCountry(int countryId, PlayerGame player) {

    }

    @Override
    public String communicationStyle(int gameId) {
        String ret;
        Game game = findById(gameId);
        ret = game.getCommunicationType().getDescription();
        return ret;
    }

    @Override
    public void assignCommonObjective(int gameId) {
        Objective objective = objectiveService.findById(16);
        Game game = findById(gameId);
        game.setObjectiveCommon(objective);
        save(game);
    }

    @Override
    public void moveArmies(CountryGameId idOrigin, CountryGameId idDestination, int troopsToMove) {

    }

    @Override
    public boolean verifyVictory(PlayerGame player, Game game) {
        return false;
    }

    @Override
    public void announceAttack(CountryGameId attackerId, CountryGameId defenderId) {

    }

    @Override
    public void assignSecretObjectives(int id) {
        Game game = gameRepository.findById(id).orElseThrow(()-> new RuntimeException("No existe la partida"));

        List<PlayerGame> players = playerService.findByGameId(game.getId());
        if (players.isEmpty()) throw new IllegalStateException("No hay jugadores en la partida");

        if (players.size() < 3 || players.size() > 6) {
            throw new IllegalStateException("La cantidad de jugadores debe estar entre 3 y 6");
        }

        List<Objective> originalObjectives = objectiveService.findAll(); // findAll ya viene filtrado el 16
        System.out.println("Objetivos disponibles (IDs): " +
                originalObjectives.stream().map(Objective::getId).toList());
        List<Objective> availableObjectives = new ArrayList<>(originalObjectives); // haciendo mutable la lista, permitiendo el shuffle
        Collections.shuffle(availableObjectives);

        for (PlayerGame playerGame : players) {
            Objective assigned = null;

            // Reintentar hasta que no se le asigne su mismo color (si el objetivo es eliminar color)
            while (!availableObjectives.isEmpty()) {
                Objective candidate = availableObjectives.remove(0);
                ProcessedObjective processed = AnalizeObjective.analizeObjective(candidate);

                if (processed.getType().equals(ObjectiveType.ARMY_COLOR)) {
                    String colorObjective = processed.getObjectiveColor();
                    if (playerGame.getColor().toString().equalsIgnoreCase(colorObjective)) {
                        continue; // Saltar si coincide con el color del playerGame
                    }
                }

                if (!objectiveService.existsById(candidate.getId())) {
                    throw new IllegalStateException("El objetivo con ID " + candidate.getId() + " no existe en la base de datos");
                }

                assigned = candidate;
                break;
            }

            if (assigned == null) throw new IllegalStateException("No se pudo asignar un objetivo válido al playerGame " + playerGame.getId());

            System.out.println("Asignando objetivo ID: " + assigned.getId() + " al playerGame " + playerGame.getId());

            playerGame.setSecretObjective(assigned);
            playerGameRepository.save(playerGame); // Persistir
        }
    }

    @Override
    public void assignOrderTurnByDice(Game game) {
        List<PlayerGame> players = playerService.findByGameId(game.getId());

        if (players == null || players.isEmpty()) {
            throw new IllegalStateException("No hay players en la partida");
        }

        Random random = new Random();
        Map<PlayerGame, Integer> throwings = new HashMap<>();

        // 1. Cada jugador tira el dado
        for (PlayerGame playerGame : players) {
            int dice = random.nextInt(6) + 1;
            // Prevenir empates reintentando si ya existe esa tirada
            while (throwings.containsValue(dice)) {
                dice = random.nextInt(6) + 1;
            }
            throwings.put(playerGame, dice);
        }

        // 2. Ordenar por mayor tirada (descendente)
        List<Map.Entry<PlayerGame, Integer>> ordered = throwings.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
                .toList();

        // 3. Asignar ordenTurno
        int order = 1;
        for (Map.Entry<PlayerGame, Integer> entry : ordered) {
            PlayerGame playerGame = entry.getKey();
            playerGame.setOrderTurn(order);
            playerGame.setTurn(order == 1);

            playerGameRepository.save(playerGame);
            order++;
        }
    }

    @Override
    public void assignInitialArmies(int gameId) {
        Game game = gameRepository.findById(gameId).orElseThrow(()-> new IllegalArgumentException("Partida no encontrada"));

        List<PlayerGame> players = playerService.findByGameId(gameId);
        int playerQuantity = players.size();

        int initArmies;
        switch (playerQuantity) {
            case 3 -> initArmies = 35;
            case 4 -> initArmies = 30;
            case 5 -> initArmies = 25;
            case 6 -> initArmies = 20;
            default -> throw new IllegalStateException("Número inválido de players para asignar ejércitos");
        }

        for (PlayerGame playerGame : players) {
            playerGame.getPlayer().setAvailableArmies(initArmies - playerGame.getCountries().size());
            playerService.update(playerGame.getPlayer()); // Persistimos el playerGame humano o bot
        }
    }

    @Override
    public void moveState(int id) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Partida no encontrada"));

        // registro en historial
        String message = registerMessageEvent.moveStateGameRegistry(id);
        historyService.registerEvent(game, message);

        if (game.getStates().getDescription().equalsIgnoreCase("FINISHED")) {
            System.out.println("La partida ya está finalizada, no se avanza el estado ni se ejecuta turno.");
            return;
        }

        // Inicializar el contexto con el estado actual
        GameContext context = new GameContext(game, this, countryGameService, cardService, playerService, gameStateService, objectiveService);

        if (Objects.equals(game.getStates().getDescription(), "preparation")) {
            // Ejecutar la lógica del turno según el estado
            context.executeTurn(game);
            // Avanzar al siguiente estado y actualizarlo en la entidad
            context.moveState(game);
            // Persistimos los cambios
            gameRepository.save(game);
        } else {
            context.moveState(game);
        }
        game = gameRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Partida no encontrada"));


        context = new GameContext(game, this, countryGameService, cardService, playerService, gameStateService, objectiveService);
        context.executeTurn(game);
        gameRepository.save(game);
        Optional<Game> updatedGame = gameRepository.findById(game.getId());
        System.out.println("Estado real en BD: " + updatedGame.get().getStates().getDescription());

    }

    @Override
    public Optional<StateGameEntity> findStateByDescription(String description) {
        return gameStateService.findByDescriptionIgnoreCase(description);
    }

    @Override
    public List<PlayerGame> getPlayerGame(int gameId) {
        return playerService.findByGameId(gameId);
    }

    @Override
    public void update(Game game) {
        try{
            gameRepository.save(game);
        }catch (Exception e){
            System.out.println("Error al actualizar el estado de la partida");
        }
    }

    @Override
    public void changeState(Game game, StateGameEnum newStateEnum) {
        StateGameEntity state = gameStateService
                .findByDescriptionIgnoreCase(newStateEnum.name())
                .orElseThrow(() -> new IllegalStateException("Estado no encontrado: " + newStateEnum.name()));

        game.setStates(state);
        gameRepository.save(game);

        // registro en historial
        String message = registerMessageEvent.changeStateGameRegistry(game, state);
        historyService.registerEvent(game, message);

    }

    public BigJsonDTO getDataGame(int gameId) {
        BigJsonDTO ret = new BigJsonDTO();

        Game game = findById(gameId);
        if (game == null) throw new NoSuchElementException("Partida no encontrada.");
        ret.setGame(new GameResponseDTO(game.getId(), game.getObjectiveCommon().getDescription(), game.getCommunicationType().getDescription(), game.getStartDate()));

        List<PlayerGame> players = getPlayerGame(game.getId());
        players = players.stream().sorted(Comparator.comparingInt(PlayerGame::getOrderTurn)).toList();
        List<PlayerGameDto> resPlayers = players.stream().map(playerService::PlayerGameToDTO).toList();
        ret.setPlayers(resPlayers);

        List<CountryGame> countries = countryGameService.findByGame(game);
        List<CountryGameDTO> resCountries = countries.stream().map(countryGameService::countryGameToDTO).toList();
        ret.setCountries(resCountries);
        return ret;
    }

    @Override
    public void startFirstTurnOfTheGame(Game game) {
        turnService.startFirstTurnOfTheGame(game);
    }



}