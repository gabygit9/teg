package ar.edu.utn.frc.tup.piii.state;

import ar.edu.utn.frc.tup.piii.model.entities.PlayerGame;
import ar.edu.utn.frc.tup.piii.model.entities.Game;
import ar.edu.utn.frc.tup.piii.model.entities.StateGameEntity;
import ar.edu.utn.frc.tup.piii.model.enums.StateGameEnum;
import ar.edu.utn.frc.tup.piii.services.interfaces.GameStateService;
import ar.edu.utn.frc.tup.piii.services.interfaces.PlayerService;
import ar.edu.utn.frc.tup.piii.services.interfaces.GameService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
public class FinishedStateGame implements StateGame {
    private PlayerService playerService;
    private GameService gameService;
    private GameStateService gameStateService;
    /**
     * TODO: Registrar el fin de la partida.
     * Podría mostrar mensaje, bloquear acciones, guardar historial, etc.
     */
    @Override
    public void executeTurn(Game game) {
        // 1. Validar estado actual
        if (!game.getStates().getDescription().equalsIgnoreCase(StateGameEnum.FINISHED.name())) {
            throw new IllegalStateException("La partida no está en estado FINALIZADA.");
        }

        // 2. Registrar el jugador winner
        List<PlayerGame> players = playerService.findByGameId(game.getId());
        PlayerGame winner = players.stream()
                .filter(PlayerGame::isObjectiveAchieved)
                .findFirst()
                .orElse(null);

        if (winner != null) {
            System.out.println("El jugador winner es: " + winner.getPlayer().getName());
            // Acá se podría guardar una entrada en la tabla HistorialPartidas
        } else {
            System.out.println("No se registró un winner.");
        }

        // 3. Registrar historial
        // historialService.registrarFin(partida, winner);

        // 4. Bloquear acciones o marcar como "inactiva"
        gameService.changeState(game, StateGameEnum.FINISHED);
        System.out.println("Finalización ejecutada para partida " + game.getId());
    }

    @Override
    public StateGameEntity moveState(Game game) {
        return gameStateService.findByDescription("FINISHED");
    }

}
