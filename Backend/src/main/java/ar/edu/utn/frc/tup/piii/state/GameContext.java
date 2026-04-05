package ar.edu.utn.frc.tup.piii.state;

import ar.edu.utn.frc.tup.piii.model.entities.*;
import ar.edu.utn.frc.tup.piii.services.interfaces.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * Clase de contexto que mantiene el estado actual de una partida
 * y permite delegar la ejecución del turno según la lógica del estado.
 *
 * Implementa el patrón State.
 *
 * @author CamachoGabriela
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GameContext {

private StateGame currentState;
private StateGameEntity stateGameEntity;
private GameService gameService;
private CountryGameService countryGameService;
private CardService cardService;
private PlayerService playerService;
private GameStateService gameStateService;
private ObjectiveService objectiveService;

    public GameContext(Game game,
                       GameService gameService,
                       CountryGameService countryGameService,
                       CardService cardService,
                       PlayerService playerService,
                       GameStateService gameStateService,
                       ObjectiveService objectiveService) {
        this.gameService = gameService;
        this.countryGameService = countryGameService;
        this.cardService = cardService;
        this.playerService = playerService;
        this.gameStateService = gameStateService;
        this.objectiveService = objectiveService;
        this.stateGameEntity = game.getStates();
        this.currentState = initState(this.stateGameEntity.getDescription());
    }

    /**
     * Asocia una implementación concreta del estado a partir del enum almacenado.
     */
    private StateGame initState(String description) {
        return switch (description.toUpperCase()){
            case "PREPARATION" -> new PreparationStateGame(gameService, countryGameService, gameStateService);
            case "FIRST_ROUND" -> new FirstRoundStateGame(playerService, gameStateService);
            case "SECOND_ROUND" -> new SecondRoundStateGame(playerService, gameStateService);
            case "HOSTILITIES" -> new HostilitiesStateGame(playerService, objectiveService, gameService, gameStateService);
            case "FINISHED" -> new FinishedStateGame(playerService, gameService, gameStateService);
            default -> throw new IllegalStateException("Estado desconocido: " + description.toUpperCase());
        };
    }

    /**
     * Ejecutar el turno actual según el estado.
     */
    public void executeTurn(Game game) {
        currentState.executeTurn(game);
    }

    /** todo funciona?? ver si se cambia el estado de las fases
     * Avanza al siguiente estado de la partida, actualiza el enum en la entidad
     * y reemplaza el estado actual en el contexto.
     */
    public void moveState(Game game) {
        String newStateDescription = currentState.moveState(game).getDescription();
        StateGameEntity newState =
                gameStateService.findByDescription(newStateDescription);

        this.stateGameEntity = newState;
        this.currentState = initState(newState.getDescription());

        game.setStates(newState);
    }

    public void updateStateFromGame(Game game) {
        this.stateGameEntity = game.getStates();
        this.currentState = initState(this.stateGameEntity.getDescription());
    }


}
