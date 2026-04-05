package ar.edu.utn.frc.tup.piii.bot;

import ar.edu.utn.frc.tup.piii.model.entities.Game;
import ar.edu.utn.frc.tup.piii.model.entities.PlayerGame;

public interface IBehaviorBot {
    void playTurn(PlayerGame player, Game game);
    void distributeArmies(PlayerGame player, Game game);
    void attack(PlayerGame player, Game game);
    void regroup(PlayerGame player, Game game);
}
