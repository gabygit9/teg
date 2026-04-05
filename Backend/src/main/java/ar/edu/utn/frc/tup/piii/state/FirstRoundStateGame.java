package ar.edu.utn.frc.tup.piii.state;

import ar.edu.utn.frc.tup.piii.model.entities.BasePlayer;
import ar.edu.utn.frc.tup.piii.model.entities.PlayerGame;
import ar.edu.utn.frc.tup.piii.model.entities.Game;
import ar.edu.utn.frc.tup.piii.model.entities.StateGameEntity;
import ar.edu.utn.frc.tup.piii.model.enums.StateGameEnum;
import ar.edu.utn.frc.tup.piii.services.interfaces.GameStateService;
import ar.edu.utn.frc.tup.piii.services.interfaces.PlayerService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
public class FirstRoundStateGame implements StateGame {
    private PlayerService playerService;
    private GameStateService gameStateService;

    /**
     * se maneja en el front hacer endpoint /finalizar-turno-ronda para avanzar tuno o estado
     */
    @Override
    public void executeTurn(Game game) {
        if (!game.getStates().getDescription().equalsIgnoreCase(StateGameEnum.FIRST_ROUND.name())) {
            throw new IllegalStateException("La partida no está en Primera Ronda.");
        }

        List<PlayerGame> players = playerService.findByGameId(game.getId());
        System.out.println("Ejecutand turno en el contexto");
        for (PlayerGame playerGame : players) {
            BasePlayer base = playerGame.getPlayer();
            if (base.getAvailableArmies() == 0) {
                base.setAvailableArmies(5);
                playerService.update(base);
                System.out.println("Jugador " + base.getName() + " recibió 5 ejércitos para la primera ronda");
            }
        }

    }
    /**
     * Avanzar a SegundaRondaState.
     */
    @Override
    public StateGameEntity moveState(Game game) {
        return gameStateService.findByDescription("SEGUNDA_RONDA");
    }

}
