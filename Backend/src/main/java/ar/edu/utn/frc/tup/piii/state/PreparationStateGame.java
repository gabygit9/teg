package ar.edu.utn.frc.tup.piii.state;

import ar.edu.utn.frc.tup.piii.model.entities.Game;
import ar.edu.utn.frc.tup.piii.model.entities.StateGameEntity;
import ar.edu.utn.frc.tup.piii.model.enums.StateGameEnum;
import ar.edu.utn.frc.tup.piii.services.interfaces.GameStateService;
import ar.edu.utn.frc.tup.piii.services.interfaces.CountryGameService;
import ar.edu.utn.frc.tup.piii.services.interfaces.GameService;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PreparationStateGame implements StateGame {
    private final GameService gameService;
    private final CountryGameService countryGameService;
    private final GameStateService gameStateService;


    /**
     * TODO: Ejecutar lógica de preparación:
     * - Asignar países aleatoriamente a los jugadores.
     * - Asignar objetivos secretos y común.
     * - Inicializar ejércitos base en cada país.
     * - Mezclar mazos.
     */
    @Override
    public void executeTurn(Game game) {
        // Validar que la partida esté en preparación
        if (!game.getStates().getDescription().equalsIgnoreCase(StateGameEnum.PREPARATION.name())) {
            throw new IllegalStateException("La partida no está en estado de preparación.");
        }

        //Asignar países a los jugadores y 1 ejército a cada pais
        countryGameService.distributeInitialCountries(game.getId());

        //Asignar objetivos secretos
        gameService.assignSecretObjectives(game.getId());

        // Asignar objetivo común
        gameService.assignCommonObjective(game.getId());

        // 5. Asignar orden de turnos con tirada de dados
        gameService.assignOrderTurnByDice(game);

        // crear primer turno de la partida
        gameService.startFirstTurnOfTheGame(game);

    }
    /**
     * TODO: Avanzar a PrimeraRondaState.
     * Debe llamar a context.setEstado(new PrimeraRondaState()) y actualizar Partida.
     */
    @Override
    public StateGameEntity moveState(Game game) {
        return gameStateService.findByDescription("FIRST_ROUND");
    }

}
