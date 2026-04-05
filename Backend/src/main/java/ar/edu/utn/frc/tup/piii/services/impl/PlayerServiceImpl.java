package ar.edu.utn.frc.tup.piii.services.impl;

import ar.edu.utn.frc.tup.piii.bot.BotNoviceStrategy;
import ar.edu.utn.frc.tup.piii.bot.IBehaviorBot;
import ar.edu.utn.frc.tup.piii.dto.BasePlayerDTO;
import ar.edu.utn.frc.tup.piii.dto.PlayerGameDto;
import ar.edu.utn.frc.tup.piii.dto.ObjectiveDto;
import ar.edu.utn.frc.tup.piii.mappers.BasePlayerMapper;
import ar.edu.utn.frc.tup.piii.mappers.PlayerGameMapper;
import ar.edu.utn.frc.tup.piii.model.entities.*;
import ar.edu.utn.frc.tup.piii.model.enums.StateGameEnum;
import ar.edu.utn.frc.tup.piii.model.enums.TurnPhase;
import ar.edu.utn.frc.tup.piii.model.repository.*;
import ar.edu.utn.frc.tup.piii.services.interfaces.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;

import static ar.edu.utn.frc.tup.piii.util.CombatUtil.countryGameService;

/**
 * Implementación del servicio de gestión de jugadores.
 * Proporciona la lógica de negocio para las operaciones CRUD de jugadores.
 * {@code @author:} Ismael Ceballos
 */
@Service
public class PlayerServiceImpl implements PlayerService {

    private final PlayerRepository playerRepository;
    private final HumanPlayerRepository humanPlayerRepository;
    private final PlayerGameRepository playerGameRepository;
    private final ColorRepository colorRepo;
    private final LevelBotRepository levelBotRepository;
    private final PlayerBotRepository playerBotRepository;
    private final TurnService turnService;
    private final UserService userService;

    private final BotNoviceStrategy botNoviceStrategy;
    private final GameService gameService;
    private final CardService cardService;
    private final PlayerGameMapper playerGameMapper;

    @Autowired
    public PlayerServiceImpl(
            PlayerRepository playerRepository,
            HumanPlayerRepository humanPlayerRepository,
            PlayerGameRepository playerGameRepository,
            ColorRepository colorRepo,
            UserService userService,
            LevelBotRepository levelBotRepository,
            PlayerBotRepository playerBotRepository,
            @Lazy GameService gameService,
            @Lazy TurnService turnService,
            @Lazy BotNoviceStrategy botNoviceStrategy, CardService cardService, PlayerGameMapper playerGameMapper) {
        this.playerRepository = playerRepository;
        this.humanPlayerRepository = humanPlayerRepository;
        this.playerGameRepository = playerGameRepository;
        this.colorRepo = colorRepo;
        this.userService = userService;
        this.gameService = gameService;
        this.levelBotRepository = levelBotRepository;
        this.playerBotRepository = playerBotRepository;
        this.turnService = turnService;
        this.botNoviceStrategy = botNoviceStrategy;
        this.cardService = cardService;
        this.playerGameMapper = playerGameMapper;
    }


    /**
     * Busca un jugador por su identificador único.
     *
     * @param id Identificador único del jugador
     * @return Optional con el jugador encontrado o vacío si no existe
     */
    @Override
    public Optional<BasePlayerDTO> findById(int id) {
        return playerRepository.findById(id).map(BasePlayerMapper::toDto);
    }

//    /**
//     * Guarda un nuevo jugador en el sistema.
//     *
//     * @param jugador Entidad jugador a guardar
//     * @return true si el guardado fue exitoso, false en caso contrario
//     */
//    @Override
//    public boolean save(JugadorBase jugador) {
//        JugadorBase guardado = jugadorRepo.save(jugador);
//        return guardado.getId() >= 0;
//    }

    @Override
    public HumanPlayer saveHumanPlayer(BasePlayerDTO basePlayerDTO) {
        HumanPlayer existent = humanPlayerRepository.findById(basePlayerDTO.getId())
                .orElseThrow(() -> new EntityNotFoundException("JugadorHumano no encontrado con ID: " + basePlayerDTO.getId()));

        existent.setAvailableArmies(basePlayerDTO.getAvailableArmies());

        return humanPlayerRepository.save(existent);
    }

    @Override
    public PlayerGame savePlayerGame(int gameId, int basePlayerId, int colorId) {
        Game game = gameService.findById(gameId);
        if (game == null) throw new NoSuchElementException("Partida no encontrada");
        BasePlayer jb = playerRepository.findById(basePlayerId)
                .orElseThrow(() -> new NoSuchElementException("Jugador no encontrado"));
        Color color = colorRepo.findById(colorId)
                .orElseThrow(() -> new NoSuchElementException("color no encontrado"));
        PlayerGame jp = new PlayerGame(
                0,
                game,
                jb,
                color,
                new Objective(16, null),
                false,
                -1,
                false,
                null,
                null,
                true
        );

        return playerGameRepository.save(jp);
    }

    @Override
    public BasePlayerDTO updateArmies(int id, int armies) {
        if(armies <0){
            throw new IllegalArgumentException("cantidadEjercito no puede ser negativo");
        }

        BasePlayer basePlayer = playerRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Jugador no encontrado"));

        basePlayer.setAvailableArmies(armies);
        BasePlayer updatedPlayer = playerRepository.save(basePlayer);

        return BasePlayerMapper.toDto(updatedPlayer);
    }


    /**
     * Obtiene todos los jugadores registrados en el sistema.
     *
     * @return Lista de todos los jugadores o lista vacía si no hay registros
     */
    @Override
    public List<BasePlayerDTO> findAll() {
        List<BasePlayer> players = playerRepository.findAllPlayers();
        return BasePlayerMapper.toDtoList(players);
    }

    @Override
    public List<PlayerGame> findByGameId(int gameId) {
        return playerGameRepository.findByGame_Id(gameId);
    }

    @Override
    public Optional<PlayerGame> findPlayerGameById(int id) {
        return playerGameRepository.findById(id);
    }

    /**
     * Actualiza la información de un jugador existente.
     *
     * @param player Entidad jugador con los datos actualizados
     * @return true si la actualización fue exitosa, false en caso contrario
     */
    @Override
    public boolean update(BasePlayer player) {
        try{
            playerRepository.save(player);
            return true;
        }catch (Exception e){
            System.out.println("Error al actualizar el jugador");
            return false;
        }
    }
    public void persistConcretPlayer(BasePlayer player) {
        if (player instanceof HumanPlayer human) {
            humanPlayerRepository.save(human);
        } else if( player instanceof BotPlayer bot){
            playerBotRepository.save(bot);
            System.out.println("ej: "+bot.getAvailableArmies());
        }
        else {
            throw new IllegalArgumentException("Tipo de jugador no soportado: " + player.getClass());
        }
    }


    public PlayerGame createHumanPlayerAndAssignToGame(BasePlayerDTO dto, String userName, int gameId, int colorId) {
        boolean alreadyExists = playerGameRepository.existsByGame_IdAndPlayer_Id(gameId, dto.getId());
        if (alreadyExists) {
            throw new IllegalStateException("El player ya está asignado a esta partida");
        }

        // 1. Buscar al usuario que representa a este player
        User user = userService.findByName(userName);
        if (user == null) throw new IllegalArgumentException("Usuario no encontrado: " + userName);

        // 2. Crear y guardar player humano
        HumanPlayer player = new HumanPlayer();
        player.setName(dto.getPlayerName());
        player.setAvailableArmies(dto.getAvailableArmies());
        player.setUser(user);

        player = humanPlayerRepository.save(player);

        // 3. Asignarlo a la partida
        Color color = colorRepo.findById(colorId)
                .orElseThrow(() -> new IllegalArgumentException("Color no encontrado"));

        Game game = gameService.findById(gameId);

        PlayerGame playerGame = new PlayerGame(
                0,
                game,
                player,
                color,
                new Objective(16, null),
                false,
                -1,
                false,
                null,
                null,
                true
        );

        return playerGameRepository.save(playerGame);
    }

    @Override
    public Optional<PlayerGame> getPlayerInTurn(int id) {
        int jpId = turnService.getPlayerInTurn(id);
        return findPlayerGameById(jpId);
    }

    @Override
    public Optional<PlayerGame> getPlayerTurnByGame(int gameId) {
        return playerGameRepository.findByGame_IdAndIsTurnIsTrue(gameId);
    }

    @Override
    public Optional<PlayerGame> getAPlayerInAGame(int gameId, int playerGameId) {
        System.out.println("Buscando jugadorPartida para partidaId={} y jugadorBaseId={}" + gameId + playerGameId);

        return playerGameRepository.findByIdAndGameId(gameId, playerGameId);
    }

    @Override
    public void save(PlayerGame playerGame) {
        playerGameRepository.save(playerGame);
    }

    @Override
    public void saveBasePlayer(BasePlayer basePlayer) {
        playerRepository.save(basePlayer);
    }

    @Override
    public PlayerGameDto createBotPlayerGame(int gameId, int difficultId, int colorId) {
        Game game = gameService.findById(gameId);
        if (game == null) throw new NoSuchElementException("Partida no encontrada.");
        Color color = colorRepo.findById(colorId).orElseThrow(() -> new NoSuchElementException("Color no encontrado."));
        LevelBot level = levelBotRepository.findById(difficultId).orElseThrow(() -> new NoSuchElementException("Nivel no encontrado."));
        BotPlayer bot = searchAvailableBot(gameId, level);

        PlayerGame jp = playerGameRepository.save(new PlayerGame(
                0,
                game,
                bot,
                color,
                new Objective(16, null),
                false,
                -1,
                false,
                null,
                null,
                true
        ));

        return new PlayerGameDto(
                jp.getId(),
                jp.getColor().getName(),
                new ObjectiveDto(jp.getSecretObjective().getId(),jp.getSecretObjective().getDescription()),
                new BasePlayerDTO(jp.getPlayer().getId(),jp.getPlayer().getName(),jp.getPlayer().getAvailableArmies()),
                false,
                false,
                false,
                0,
                ""
                );
    }

    @Override
    public PlayerGameDto assignHumanInGame(int gameId, String name, int colorId) {
        User user = userService.findByName(name);
        if (user == null) throw new NoSuchElementException("Usuario no encontrado.");
        BasePlayer jBase = playerRepository.findByName(name);
        if (jBase != null) {
            return PlayerGameToDTO(savePlayerGame(gameId, jBase.getId(), colorId));
        }
        HumanPlayer human = new HumanPlayer(user);
        human.setId(0);
        human.setAvailableArmies(0);
        human.setName(user.getName());
        int id = humanPlayerRepository.save(human).getId();

        return PlayerGameToDTO(savePlayerGame(gameId, id, colorId));
    }

    @Override
    public PlayerGameDto PlayerGameToDTO(PlayerGame jp) {
        BasePlayerDTO retJbase = new BasePlayerDTO(jp.getPlayer().getId(),jp.getPlayer().getName(),jp.getPlayer().getAvailableArmies());
        ObjectiveDto objRet = new ObjectiveDto(jp.getSecretObjective().getId(), jp.getSecretObjective().getDescription());
        return new PlayerGameDto(jp.getId(), jp.getColor().getName(), objRet, retJbase, true, false, false,0,"");
    }

    @Deprecated
    @Override
    public List<PlayerGame> findAllPlayersGame() {
        return playerGameRepository.findAll();
    }

    public BotPlayer searchAvailableBot(int gameId, LevelBot difficult) {
        List<BasePlayer> players = findByGameId(gameId).stream().map(PlayerGame::getPlayer).toList();
        if (players.size() == 6) throw new IllegalArgumentException("Partida con cantidad máxima de players.");
        List<BotPlayer> bots = playerBotRepository.findByLevelBot(difficult);
        Random random = new Random();
        BotPlayer ret;
        boolean found;

        do {
            int randomIndex = random.nextInt(5);
            ret = bots.get(randomIndex);
            found = false;
            for (BasePlayer jp: players) {
                if (jp.getId() == ret.getId()) {
                    found = true;
                    break;
                }
            }
        } while (found);

        return ret;
    }


    public Boolean executeTurnBot(int playerGameId, int gameId, int turnId) {

        Game game = gameService.findById(gameId);
        if (game == null) throw new NoSuchElementException("partida no encontrada en ejecutarTurnoBot");
        PlayerGame playerBot = playerGameRepository.findById(playerGameId)
                .orElseThrow(()-> new NoSuchElementException("Jugador Bot no encontrado en ejecutarTurnoBot"));
        Turn turn = turnService.findById(turnId);
        if (turn == null) throw new NoSuchElementException("turno no encontrado en ejecutarTurnoBot");
        if (turn.getPlayerGame() != playerBot) throw new IllegalArgumentException("El turno no coincide con con el bot");

        IBehaviorBot strategy = getStrategyBot(playerBot);

        strategy.playTurn(playerBot, game);

        boolean ret = false;

        if (game.getStates().getDescription().equalsIgnoreCase(StateGameEnum.HOSTILITIES.toString())) {
            turnService.movePhase(turn);
            ret = turn.getCurrentPhase() != TurnPhase.REGROUPING;
        }

        return ret;
    }

    private IBehaviorBot getStrategyBot(PlayerGame playerBot) {
        return botNoviceStrategy;
    }


    @Override
    public BasePlayerDTO findBasePlayerPerUserId(int userId) {
        HumanPlayer jh = humanPlayerRepository.findByUser_Id(userId);
        return new BasePlayerDTO(jh.getId(), jh.getName(), jh.getAvailableArmies());
    }

    @Override
    public boolean wasEliminatedColor(String colorObjective, int id) {
        Optional<PlayerGame> playerObjectiveOpt = playerGameRepository.findByGame_IdAndColor_Name(id, colorObjective);
        if (playerObjectiveOpt.isEmpty()) return false;

        PlayerGame objectivePlayer = playerObjectiveOpt.get();

        if (!objectivePlayer.isActive()) return true;

        List<CountryGame> countries = countryGameService.findByGameAndPlayerGame(id, objectivePlayer.getId());

        return countries.isEmpty();
    }

}