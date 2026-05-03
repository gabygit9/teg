package ar.edu.utn.frc.tup.piii.services.impl;

import ar.edu.utn.frc.tup.piii.dto.*;
import ar.edu.utn.frc.tup.piii.model.entities.*;
import ar.edu.utn.frc.tup.piii.model.enums.StateGameEnum;
import ar.edu.utn.frc.tup.piii.model.repository.CommunicationTypeRepository;
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
    private final CommunicationTypeRepository communicationTypeRepository;


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
        if (game == null) throw new IllegalArgumentException("GameDTO is null");

        // Allow fallback: frontend may send nested objects (state, commonObjective) instead of raw ids
        int stateId = game.getStateId();
        if (stateId == 0 && game.getState() != null) {
            stateId = game.getState().getId();
        }

        int objectiveId = game.getCommonObjectiveId();
        if (objectiveId == 0 && game.getCommonObjective() != null) {
            objectiveId = game.getCommonObjective().getId();
        }

        // If stateId is not provided, fallback to default 'preparation' state
        StateGameEntity estado;
        if (stateId <= 0) {
            estado = gameStateService.findByDescription("preparation");
            if (estado == null) throw new IllegalArgumentException("Default state 'preparation' not found in DB");
        } else {
            estado = gameStateService.findById(stateId);
            if (estado == null) throw new IllegalArgumentException("State with id " + stateId + " not found");
        }

        if (objectiveId <= 0) throw new IllegalArgumentException("commonObjectiveId is required and must be > 0");

        // Try to find the requested objective; if not found, try goal id 16, then first available objective
        Objective objective = objectiveService.findById(objectiveId);
        if (objective == null) {
            System.out.println("Objective with id " + objectiveId + " not found. Trying default id 16 and then any available objective.");
            objective = objectiveService.findById(16);
        }
        if (objective == null) {
            List<Objective> all = objectiveService.findAll();
            if (all != null && !all.isEmpty()) {
                objective = all.get(0);
                System.out.println("Falling back to first available objective with id " + objective.getId());
            }
        }
        if (objective == null) throw new IllegalArgumentException("No objective available in DB to assign to the game");

        if (game.getCommunicationType() == null || game.getCommunicationType().getId() <= 0) {
            throw new IllegalArgumentException("CommunicationType id is required in GameDTO");
        }

        int commId = game.getCommunicationType().getId();
        CommunicationType communicationType = communicationTypeRepository.findById(commId).orElse(null);
        if (communicationType == null) {
            // No existe el tipo en la BD: crearlo automáticamente usando la descripción recibida
            String desc = game.getCommunicationType().getDescription();
            if (desc == null || desc.isBlank()) {
                desc = "DEFAULT_COMMUNICATION"; // fallback si no se envía descripción
            }
            System.out.println("CommunicationType with id " + commId + " not found in DB. Creating new with description='" + desc + "'.");
