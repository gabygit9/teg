package ar.edu.utn.frc.tup.piii.services.impl;

import ar.edu.utn.frc.tup.piii.dto.*;
import ar.edu.utn.frc.tup.piii.dto.mementosDTOs.*;
import ar.edu.utn.frc.tup.piii.mappers.mementoMappers.*;
import ar.edu.utn.frc.tup.piii.model.entities.*;
import ar.edu.utn.frc.tup.piii.model.repository.*;
import ar.edu.utn.frc.tup.piii.services.interfaces.GameMementoService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class GameMementoServiceImpl implements GameMementoService {

    @Autowired
    private GameMementoRepository gameMementoRepository;

    @Autowired
    private PlayerGameRepository playerGameRepository;

    @Autowired
    private CountryGameRepository countryGameRepository;

    @Autowired
    private TurnRepository turnRepository;

    @Autowired
    private PactRepository pactRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private HistoryRepository historyEventRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private ColorRepository colorRepository;

    @Autowired
    private ObjectiveRepository objectiveRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private PactTypeRepository pactTypeRepository;



    private final ObjectMapper objectMapper;

    public GameMementoServiceImpl() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }


    // Obtiene todos los mementos de una partida según su ID.
    @Override
    public List<GameMemento> getStatesByGame(int gameId) {
        return gameMementoRepository.findByGameId(gameId);
    }


    // Obtiene el último estado guardado de una partida por fecha
    @Override
    public GameMemento getLastState(int gameId) {
        return gameMementoRepository.findTopByGameIdOrderByDateTimeDesc(gameId);
    }

    private void validateState(GameStateMementoDTO state) {
        if (state.getGame() == null || state.getGame().getId() == 0) {
            throw new IllegalArgumentException("El state no contiene una partida válida.");
        }
        if (state.getPlayers() == null || state.getPlayers().isEmpty()) {
            throw new IllegalArgumentException("El state no contiene jugadores.");
        }
        for (var player : state.getPlayers()) {
            if (player.getBasePlayerId() <= 0) {
                throw new IllegalArgumentException("Jugador con ID inválido: " + player.getId());
            }
        }
        if (state.getCountries() == null || state.getCountries().isEmpty()) {
            throw new IllegalArgumentException("El state no contiene países.");
        }
        for (var country : state.getCountries()) {
            if (country.getPlayerGameId() <= 0 || country.getCountryId() <= 0) {
                throw new IllegalArgumentException("PaisPartida con datos inválidos. PaisId: " +
                        country.getCountryId() + ", jugadorPartidaId: " + country.getPlayerGameId());
            }
        }
    }


    // Guarda un nuevo estado completo del juego
    // el estado se serializa como JSON y se guarda en la base de datos
    @Transactional
    public GameMemento saveMementoComplete(Game game, int version) {

        List<PlayerGame> playerEntity = playerGameRepository.findByGame_Id(game.getId());
        List<CountryGame> countryEntity = countryGameRepository.findByGame(game);
        List<Turn> turns = turnRepository.findByGame_Id(game.getId());
        List<Pact> pacts = pactRepository.findByGame_Id(game.getId());
        List<Message> messages = messageRepository.findByGame_Id(game.getId());
        List<HistoryEvent> historyEvents = historyEventRepository.findByGame_Id(game.getId());

        // Convertir a DTOs para evitar referencias circulares
        List<PlayerGameMementoDTO> players = playerEntity.stream()
                .map(PlayerGameMementoMapper::toDTO)
                .toList();

        List<CountryGameMementoDTO> countries = countryEntity.stream()
                .map(CountryGameMementoMapper::toDTO)
                .toList();

        List<TurnMementoDTO> turnsDTO = turns.stream()
                .map(TurnMementoMapper::toDTO)
                .toList();

        List<PactMementoDTO> pactsDTO = pacts.stream()
                .map(PactMementoMapper::toDTO)
                .toList();

        List<MessageMementoDTO> messagesDTO = messages.stream()
                .map(MessageMementoMapper::toDTO)
                .toList();

        List<HistoryMementoDTO> historyDTO = historyEvents.stream()
                .map(HistoryMementoMapper::toDTO)
                .toList();

        GameStateMementoDTO estado = new GameStateMementoDTO(
                game,
                players,
                countries,
                turnsDTO,
                pactsDTO,
                messagesDTO,
                historyDTO
        );

        try {
            String json = objectMapper.writeValueAsString(estado);

            GameMemento memento = new GameMemento();
            memento.setGame(game);
            memento.setDateTime(LocalDateTime.now());
            memento.setStateSerialized(json);
            memento.setVersion(version);

            return gameMementoRepository.save(memento);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error al serializar el estado del juego", e);
        }
    }






    // Restaura un estado de juego a partir de un memento previamente guardado
    @Transactional
    public GameStateMementoDTO restoreAndPersistState(int mementoId) {
        GameMemento memento = gameMementoRepository.findById(mementoId)
                .orElseThrow(() -> new RuntimeException("Memento no encontrado"));

        GameStateMementoDTO state;
        try {
            state = objectMapper.readValue(memento.getStateSerialized(), GameStateMementoDTO.class);
            validateState(state);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error al deserializar el state JSON", e);
        }

        int gameId = state.getGame().getId();
        Game gameManaged = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Partida no encontrada"));

        // Borrar datos actuales relacionados con la partida
        pactRepository.deleteByGameId(gameId);
        messageRepository.deleteByGameId(gameId);
        historyEventRepository.deleteByGameId(gameId);
        turnRepository.deleteByGameId(gameId);
        countryGameRepository.deleteByGameId(gameId);
        playerGameRepository.deleteByGameId(gameId);

        // Reconstruir playerGames
        List<PlayerGame> playerGames = state.getPlayers().stream()
                .map(dto -> {
                    if (dto.getBasePlayerId() <= 0) {
                        throw new RuntimeException("jugadorBaseId inválido para jugadorPartidaId " + dto.getId());
                    }
                    var basePlayer = playerRepository.findById(dto.getBasePlayerId())
                            .orElseThrow(() -> new RuntimeException("JugadorBase no encontrado con id " + dto.getBasePlayerId()));

                    PlayerGame playerGame = new PlayerGame();
                    playerGame.setGame(gameManaged);
                    playerGame.setPlayer(basePlayer);
                    playerGame.setColor(colorRepository.findById(dto.getColorId()).orElse(null));
                    playerGame.setSecretObjective(objectiveRepository.findById(dto.getObjectiveId()).orElse(null));
                    playerGame.setObjectiveAchieved(dto.isObjectiveAchieved());
                    playerGame.setOrderTurn(dto.getOrderTurn());
                    playerGame.setTurn(dto.isTurn());
                    playerGame.setActive(dto.isActive());
                    return playerGame;
                })
                .toList();

        List<PlayerGame> persistedPlayers = playerGameRepository.saveAll(playerGames);
        Map<Integer, PlayerGame> mapIdOldToNew = new HashMap<>();
        for (int i = 0; i < state.getPlayers().size(); i++) {
            Integer oldId = state.getPlayers().get(i).getId();
            PlayerGame newPlayer = persistedPlayers.get(i);
            mapIdOldToNew.put(oldId, newPlayer);
        }

        // Reconstruir países
        List<CountryGame> countryGames = state.getCountries().stream()
                .map(dto -> {
                    CountryGameId id = new CountryGameId(dto.getCountryId(), gameId);

                    CountryGame countryGame = countryGameRepository.findById(id).orElseGet(() -> {
                        CountryGame newGame = new CountryGame();
                        newGame.setId(id);
                        return newGame;
                    });

                    countryGame.setGame(gameManaged);
                    countryGame.setCountry(countryRepository.findById(dto.getCountryId()).orElse(null));
                    countryGame.setAmountArmies(dto.getAvailableArmies());

                    if (dto.getPlayerGameId() == 0) {
                        throw new RuntimeException("jugadorPartidaId es null para PaisPartida con paisId " + dto.getCountryId());
                    }

                    PlayerGame relationPlayer = mapIdOldToNew.get(dto.getPlayerGameId());
                    if (relationPlayer == null) {
                        throw new RuntimeException("No se encontró JugadorPartida para id " + dto.getPlayerGameId());
                    }

                    countryGame.setPlayerGame(relationPlayer);
                    return countryGame;
                })
                .toList();

        countryGameRepository.saveAll(countryGames);

        // Reconstruir turnos desde TurnoMementoDTO
        state.getTurns().forEach(t -> {
            Turn turn = (t.getId() != 0) ? turnRepository.findById(t.getId()).orElse(new Turn()) : new Turn();
            turn.setInitialStartDate(t.getDateStartTurn());
            turn.setCurrentPhase(t.getCurrentPhase());
            turn.setMaxDuration(t.getMaximunDuration());
            turn.setAvailableArmies(t.getAvailableArmies());
            turn.setFinished(t.isFinished());
            turn.setGame(gameManaged);

            // Buscar y setear jugadorPartida asociado (si está en el DTO)
            if (t.getPlayerGameId() != 0) {
                PlayerGame turnPlayer = mapIdOldToNew.get(t.getPlayerGameId());
                if (turnPlayer != null) {
                    turn.setPlayerGame(turnPlayer);
                } else {
                    // Si no está, buscar en DB como fallback
                    turn.setPlayerGame(playerGameRepository.findById(t.getPlayerGameId()).orElse(null));
                }
            } else {
                turn.setPlayerGame(null);
            }

            turnRepository.save(turn);
        });

// Reconstruir pactos desde PactoMementoDTO
        state.getPacts().forEach(p -> {
            Pact pact = (p.getId() != 0) ? pactRepository.findById(p.getId()).orElse(new Pact()) : new Pact();
            pact.setGame(gameManaged);

            // TipoPacto (queda igual porque tiene ID)
            if (p.getPactType() != null && !p.getPactType().isEmpty()) {
                // Buscar TipoPacto por nombre o código según cómo lo tengas
                PactType type = pactTypeRepository.findByDescription(p.getPactType());
                pact.setPactType(type);
            } else {
                pact.setPactType(null);
            }


            // Jugadores del pacto (buscando en jugadorRepository por nombre)
            if (p.getPlayerNames() != null && !p.getPlayerNames().isEmpty()) {
                List<BasePlayer> managedPlayers = p.getPlayerNames().stream()
                        .map(playerName -> playerRepository.findByName(playerName))
                        .filter(java.util.Objects::nonNull)
                        .toList();

                pact.setPlayers(managedPlayers);
            } else {
                pact.setPlayers(List.of());
            }

            // Países del pacto (buscando en paisRepository por nombre)
            if (p.getCountryNames() != null && !p.getCountryNames().isEmpty()) {
                List<Country> managedCountries = p.getCountryNames().stream()
                        .map(countryName -> countryRepository.findByName(countryName).orElse(null))
                        .filter(java.util.Objects::nonNull)
                        .toList();
                pact.setCountries(managedCountries);
            } else {
                pact.setCountries(List.of());
            }

            // Jugador creador (buscando en jugadorRepository por nombre)
            if (p.getCreatorName() != null && !p.getCreatorName().isBlank()) {
                pact.setPlayerCreated(playerRepository.findByName(p.getCreatorName()));
            } else {
                pact.setPlayerCreated(null);
            }

            pact.setActive(p.isActive());
            pact.setDateTime(p.getDateTime());

            pactRepository.save(pact);
        });

// Restaurar mensajes desde MensajeMementoDTO
        state.getMessages().forEach(m -> {
            Message message = (m.getId() != 0) ? messageRepository.findById(m.getId()).orElse(new Message()) : new Message();
            message.setGame(gameManaged);

            if (m.getSenderName() != null && !m.getSenderName().isBlank()) {
                message.setSender(playerRepository.findByName(m.getSenderName()));
            } else {
                message.setSender(null);
            }

            message.setContent(m.getContent());
            message.setActiveState(m.isActiveState());
            message.setModified(m.isModified());
            message.setDatetime(m.getDateTime());

            messageRepository.save(message);
        });


        // Guardar la partida
        gameRepository.save(gameManaged);

        return state;
    }


    public List<SaveGameRequestDTO> listSaveGames() {
        List<Game> games = gameRepository.findAll();

        List<SaveGameRequestDTO> savedGames = new ArrayList<>();

        for (Game game : games) {
            Optional<GameMemento> lastMementoOpt = gameMementoRepository
                    .findTopByGameOrderByDateTimeDesc(game);

            lastMementoOpt.ifPresent(lastMemento -> savedGames.add(new SaveGameRequestDTO(
                    game.getId(),
                    lastMemento.getMementoId(),
                    lastMemento.getDateTime()
            )));
        }
        return savedGames;
    }
}