package ar.edu.utn.frc.tup.piii.state;

import ar.edu.utn.frc.tup.piii.model.entities.Game;
import ar.edu.utn.frc.tup.piii.model.entities.BasePlayer;
import ar.edu.utn.frc.tup.piii.model.entities.PlayerGame;
import ar.edu.utn.frc.tup.piii.model.entities.StateGameEntity;
import ar.edu.utn.frc.tup.piii.model.enums.StateGameEnum;
import ar.edu.utn.frc.tup.piii.services.interfaces.GameStateService;
import ar.edu.utn.frc.tup.piii.services.interfaces.PlayerService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
public class SecondRoundStateGame implements StateGame {
    private PlayerService playerService;
    private GameStateService gameStateService;
    /**
     * TODO: Dar 3 ejércitos a cada jugador y permitir su colocación.
     * Verificar fin de la ronda para avanzar a hostilidades.
     */
    @Override
    public void executeTurn(Game game) {
        if (!game.getStates().getDescription().equalsIgnoreCase(StateGameEnum.SECOND_ROUND.name())) {
            throw new IllegalStateException("La partida no está en Segunda Ronda.");
        }

        List<PlayerGame> players = playerService.findByGameId(game.getId());
        for (PlayerGame player : players) {
            BasePlayer base = player.getPlayer();

            base.setAvailableArmies(base.getAvailableArmies() + 3);
            playerService.update(base);
//            if (base.getAvailableArmies() == 0) {
//                base.setAvailableArmies(3);
//                playerService.persistConcretPlayer(base);
//                System.out.println("Jugador " + base.getNombre() + " recibió 3 ejércitos para la segunda ronda");
//            }
        }
    }

    @Override
    public StateGameEntity moveState(Game game) {
        return gameStateService.findByDescription("HOSTILITIES");
    }

}
