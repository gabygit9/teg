package ar.edu.utn.frc.tup.piii.services.impl;

import ar.edu.utn.frc.tup.piii.dto.CountryGameDTO;
import ar.edu.utn.frc.tup.piii.mappers.CountryGameMapper;
import ar.edu.utn.frc.tup.piii.model.entities.*;
import ar.edu.utn.frc.tup.piii.model.repository.*;
import ar.edu.utn.frc.tup.piii.services.interfaces.HistoryService;
import ar.edu.utn.frc.tup.piii.services.interfaces.PlayerService;
import ar.edu.utn.frc.tup.piii.services.interfaces.CountryGameService;
import ar.edu.utn.frc.tup.piii.services.interfaces.GameService;
import ar.edu.utn.frc.tup.piii.util.RegisterMessageEvent;
import jakarta.transaction.Transactional;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de gestión de países en partidas.
 * Administra la relación dinámica entre países, jugadores y partidas,
 * registrando cantidad de ejércitos y dominio en tiempo real.
 *
 * @author Ismael Ceballos
 */
@Service
@Transactional
public class CountryGameServiceImpl implements CountryGameService {

    private final CountryGameRepository countryGameRepository;

    private final CountryConnectionRepository countryConnectionRepository;

    private final GameService gameService;

    private final PlayerService playerService;

    private final CountryRepository countryRepository;

    private final RegisterMessageEvent registerMessageEvent;
    private final HistoryService historyService;


    @Autowired
    public CountryGameServiceImpl(
            CountryGameRepository countryGameRepository,
            CountryConnectionRepository countryConnectionRepository,
            @Lazy GameService gameService,
            @Lazy PlayerService playerService,
            CountryRepository countryRepository, RegisterMessageEvent registerMessageEvent, HistoryService historyService) {
        this.countryGameRepository = countryGameRepository;
        this.countryConnectionRepository = countryConnectionRepository;
        this.gameService = gameService;
        this.playerService = playerService;
        this.countryRepository = countryRepository;
        this.registerMessageEvent = registerMessageEvent;
        this.historyService = historyService;
    }

    @Override
    public boolean save(CountryGame game){
        if(game == null){return false;}
        countryGameRepository.save(game);
        return true;
    }

    @Override
    public boolean update(CountryGame game) {
        if(game == null){return false;}
        if (countryGameRepository.existsById(game.getId())) {
            countryGameRepository.save(game);
            return true;
        }
        return false;
    }

    @Override
    public List<CountryGame> findAll() {
        return countryGameRepository.findAll();
    }

    /**
     * Busca una relación por ID compuesto.
     *
     * @return entidad si existe, o null si no.
     */
    @Transactional
    @Override
    public CountryGame findById(int countryId, int gameId) {
        try {
            System.out.println("Buscando PaisPartida: paisId={}, partidaId={}"+ countryId +" "+ gameId);
            return countryGameRepository.findByIdWithAll(countryId, gameId)
                    .orElseThrow(() -> new IllegalArgumentException("No se encontró el país"));
        } catch (Exception e) {
            System.out.println("Fallo findByIdConTodo, reintentando con método alternativo..."+ e);
            Optional<CountryGame> result = countryGameRepository.findByIdSimple(countryId, gameId);
            if (result.isPresent()) {
                CountryGame pp = result.get();
                Hibernate.initialize(pp.getCountry());
                Hibernate.initialize(pp.getGame());
                Hibernate.initialize(pp.getPlayerGame());
                return pp;
            } else {
                throw new IllegalArgumentException("No se encontró el país");
            }

        }
    }

    @Override
    public List<CountryGame> findByPlayerGame(PlayerGame playerGame) {
        if(playerGame == null){return Collections.emptyList();}
        return countryGameRepository.findByPlayerGame(playerGame);
    }

    @Override
    public List<CountryGame> findByGameAndPlayerGame(int game, int player) {

        return countryGameRepository.findByGame_IdAndPlayerGame_Id(game, player);
    }

    @Override
    public List<CountryGame> findByGame(Game game) {
        if(game == null) return Collections.emptyList();
        return countryGameRepository.findByGame(game);
    }

    @Override
    public boolean increaseArmies(CountryGameId id, int quantity) {
        if (id == null || quantity <= 0) return false;
        CountryGame countryGame = countryGameRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("País no encontrado"));
        BasePlayer player = countryGame.getPlayerGame().getPlayer();

        if(player.getAvailableArmies() < quantity){
            throw new IllegalArgumentException("El player no tiene suficientes ejércitos disponibles");
        }

        //restar los ejercitos del player
        player.setAvailableArmies(player.getAvailableArmies() - quantity);

        //sumar ejercitos al pais
        countryGame.setAmountArmies(countryGame.getAmountArmies() + quantity);

        countryGameRepository.save(countryGame);
        if (player instanceof HumanPlayer) {
            playerService.persistConcretPlayer(player);
            playerService.savePlayerGame(countryGame.getGame().getId(), countryGame.getPlayerGame().getPlayer().getId(), countryGame.getPlayerGame().getColor().getId());
        }else if(player instanceof BotPlayer){
            playerService.persistConcretPlayer(player);
            playerService.savePlayerGame(countryGame.getGame().getId(), countryGame.getPlayerGame().getPlayer().getId(), countryGame.getPlayerGame().getColor().getId());
        }else {

            throw new IllegalArgumentException("Tipo de player no soportado: " + player.getClass());
        }

        // registro historial
        String message = RegisterMessageEvent.increaseArmies(player, countryGame.getCountry().getName(), quantity);
        historyService.registerEvent(countryGame.getGame(), message);

        return true;


    }

    @Override
    public boolean reduceArmies(CountryGameId id, int quantity) {
        if (id == null || quantity <= 0) return false;

        CountryGame countryGame = countryGameRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("País no encontrado"));

        int currentArmies = countryGame.getAmountArmies();

        if (currentArmies - quantity < 1) {
            throw new IllegalArgumentException("Debe permanecer aunque sea 1 ejército en el país");
        }
        countryGame.setAmountArmies(currentArmies - quantity);
        countryGameRepository.save(countryGame);
        return true;
    }

    @Override
    public List<CountryGame> findEnemyNeighbors(int idPais, PlayerGame playerGame, Game game) {
        CountryGame currentCountry = countryGameRepository.findById(new CountryGameId(idPais, game.getId())).orElse(null);

        if(currentCountry == null) return Collections.emptyList();

        List<CountryGame> allCountries = countryGameRepository.findByGame(game);
        return allCountries.stream()
                .filter(p -> p.getPlayerGame().getPlayer().getId() != playerGame.getPlayer().getId()) //enemigos
                .filter(v -> isBordering(currentCountry.getCountry().getId(), v.getCountry().getId())) //vecinos
                .toList();
    }

    @Override
    public CountryGame[] getBorder(CountryGame current, List<CountryGame> allCountries) {
        if(current == null || allCountries.isEmpty() || allCountries == null) return new CountryGame[0];

        return allCountries.stream()
                .filter(p -> isBordering(current.getCountry().getId(), p.getCountry().getId()))
                        .toArray(CountryGame[]::new);
    }

    @Override
    public boolean isBordering(int id, int id1) {
        return id != id1 && countryConnectionRepository.existsConnection(id, id1).isPresent();
    }

    @Override
    public List<CountryGameDTO> getCountriesOfGame(int gameId) {
        if(gameId < 0) return null;

        Game game = gameService.findById(gameId);

        if (game == null) {
            throw new IllegalArgumentException("Partida no encontrada");
        }

        List<CountryGame> countries = countryGameRepository.findByGame(game);
        return countries.stream()
                .map((CountryGameMapper::toDto))
                .collect(Collectors.toList());
    }

    @Override
    public List<CountryGame> distributeInitialCountries(int gameId) {
        Game game = gameService.findById(gameId);

        if (game == null) {
            throw new IllegalArgumentException("Partida no encontrada");
        }

        List<Country> allCountries = countryRepository.findAll();
        Collections.shuffle(allCountries);
        List<PlayerGame> playerGames = playerService.findByGameId(gameId);

        if (playerGames.isEmpty()) throw new IllegalStateException("No hay playerGames en la partida.");
        if (playerGames.size() > 6 || playerGames.size() < 3) throw new IllegalStateException("cantidad de playerGames inválida");
        int playerIndex = 0;
        int counter = switch (playerGames.size()) {
            case 3 -> 16;
            case 4 -> 12;
            case 5 -> 10;
            case 6 -> 8;
            default -> throw new IllegalArgumentException("Error al contar los playerGames");
        };
        final int amountCardsPerPlayer = counter;
        List<CountryGame> ret = new ArrayList<>();
        PlayerGame playerGame;
        Random random = new Random();
        for (int i = 0; i < 50; i++) {
            if (i >= allCountries.size()) break;

            Country country = allCountries.get(i);

            CountryGameId id = new CountryGameId(country.getId(), game.getId());

            CountryGame countryGame = new CountryGame();
            if (playerGames.size() != 5 && i > 47) {
                int randomIndex = random.nextInt(playerGames.size());
                playerGame = playerGames.get(randomIndex);
            } else {
                playerGame = playerGames.get(playerIndex);
            }
            countryGame.setId(id);
            countryGame.setCountry(country);
            countryGame.setGame(game);
            countryGame.setPlayerGame(playerGame);
            countryGame.setAmountArmies(1);

            CountryGame newCountryGame = countryGameRepository.save(countryGame);
            ret.add(newCountryGame);

            counter--;
            if (counter == 0) {
                playerIndex ++;
                counter = amountCardsPerPlayer;
            }
        }
        return ret;
    }

    @Override
    public CountryGame findByCountryAndGameId(Country country, int gameId) {
        return countryGameRepository.findByCountryAndGameId(country, gameId);
    }


    @Override
    public boolean checkVictory(PlayerGame playerGame, Game game) {
        boolean ret = false;

        //si la partida del jugador y el parámetro partida no son iguales
        if (!playerGame.getGame().equals(game)) return false;

        return playerGame.isObjectiveAchieved() ||
                countryGameRepository.findByPlayerGame(playerGame).size() >= 30;
    }

    @Override
    public CountryGameDTO countryGameToDTO(CountryGame pp) {
       return new CountryGameDTO(
               pp.getCountry().getId(),
               pp.getGame().getId(),
               pp.getCountry().getName(),
               pp.getCountry().getContinent().getName(),
               pp.getAmountArmies(),
               pp.getPlayerGame().getId(),
               pp.getPlayerGame().getPlayer().getName(),
               pp.getPlayerGame().getColor().getName());
    }

    @Override
    public boolean hasNBorderCountriesEachOther(PlayerGame playerGame, Integer GlobalQuantityCountries) {
        List<CountryGame> playerCountries = findByPlayerGame(playerGame);

        Map<Integer, CountryGame> map = playerCountries.stream()
                .collect(Collectors.toMap(pp -> pp.getCountry().getId(), pp -> pp));

        for (CountryGame origin : playerCountries) {
            Set<Integer> visited = new HashSet<>();
            dfsBorderings(origin, map, visited);

            if (visited.size() >= GlobalQuantityCountries) return true;
        }
        return false;
    }

    @Override
    public boolean controlCountry(PlayerGame playerGame, String country) {
        if (country == null || playerGame == null) return false;

        List<CountryGame> countryPlayers = countryGameRepository.findByPlayerGame(playerGame);

        return countryPlayers.stream()
                .anyMatch(pp -> country.equalsIgnoreCase(pp.getCountry().getName()));
    }

    public void dfsBorderings(CountryGame current, Map<Integer, CountryGame> map, Set<Integer> visited) {
        int currentId = current.getCountry().getId();
        if (visited.contains(currentId)) return;
        visited.add(currentId);

        CountryGame[] neighbors = getBorder(current, new ArrayList<>(map.values()));
        for (CountryGame neighbor : neighbors) {
            dfsBorderings(neighbor, map, visited);
        }
    }

}


