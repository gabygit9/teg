package ar.edu.utn.frc.tup.piii.state;

import ar.edu.utn.frc.tup.piii.model.entities.*;
import ar.edu.utn.frc.tup.piii.model.enums.StateGameEnum;
import ar.edu.utn.frc.tup.piii.services.interfaces.GameStateService;
import ar.edu.utn.frc.tup.piii.services.interfaces.PlayerService;
import ar.edu.utn.frc.tup.piii.services.interfaces.ObjectiveService;
import ar.edu.utn.frc.tup.piii.services.interfaces.GameService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
public class HostilitiesStateGame implements StateGame {
    private PlayerService playerService;
    private ObjectiveService objectiveService;
    private GameService gameService;
    private GameStateService gameStateService;

    /**
     * TODO: Gestionar turno completo por jugador:
     * - Incorporación de tropas
     * - Ataques
     * - Reagrupamiento
     * - Pedido de tarjeta si corresponde
     * - Verificar victoria
     */
    @Override
    public void executeTurn(Game game) {
        if (!game.getStates().getDescription().equalsIgnoreCase(StateGameEnum.HOSTILITIES.name())) {
            throw new IllegalStateException("La partida no está en fase de hostilidades.");
        }

        List<PlayerGame> players = playerService.findByGameId(game.getId());
        PlayerGame playerInTurn = getPlayerInTurn(players);

        BasePlayer basePlayer = playerInTurn.getPlayer();

        System.out.println("HOSTILIDADES funciona: " + basePlayer.getName());
        boolean achieved = objectiveService.ObjectiveAchieved(playerInTurn);
        System.out.println("¿Objetivo achieved? " + achieved);
        System.out.println("Objetivo del jugador: " + playerInTurn.getSecretObjective().getDescription());
        System.out.println("Color del jugador: " + playerInTurn.getColor());


        if (objectiveService.ObjectiveAchieved(playerInTurn) || playerInTurn.isObjectiveAchieved()) {
            playerInTurn.setObjectiveAchieved(true);
            playerService.save(playerInTurn);
            StateGameEntity finishedState = gameStateService.findByDescription("FINISHED");
            game.setStates(finishedState);
            gameService.save(game);
            System.out.println("Nuevo estado: " + game.getStates().getDescription());
            System.out.println("¡El jugador " + basePlayer.getName() + " ha achieved su objetivo y ganó la partida!");
        }
    }

    private PlayerGame getPlayerInTurn(List<PlayerGame> players) {
        return players.stream()
                .filter(PlayerGame::isTurn)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No hay jugador activo en este turno"));
    }

    /**
     * TODO: Si alguien ganó, pasar a FinalizadaState.
     * Si no, simplemente cambiar de turno (mismo estado).
     */
    @Override
    public StateGameEntity moveState(Game game) {
        if (someoneWin(game)) {
            return someoneWin(game)
                    ? gameStateService.findByDescription("FINISHED")
                    : gameStateService.findByDescription("HOSTILITIES");
        }

        return gameStateService.findByDescription("HOSTILITIES");
    }


    private boolean someoneWin(Game game) {
        return playerService.findByGameId( game.getId())
                .stream()
                .anyMatch(PlayerGame::isObjectiveAchieved);
    }
}
