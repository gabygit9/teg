package ar.edu.utn.frc.tup.piii.services.interfaces;

import ar.edu.utn.frc.tup.piii.model.entities.Objective;
import ar.edu.utn.frc.tup.piii.model.entities.PlayerGame;

import java.util.List;

/**
 * Interfaz que define las operaciones relacionadas con la gestión de objetivos del juego.
 *
 * Permite asignar, consultar y verificar el cumplimiento de los objetivos individuales y comunes.
 *
 * @author Ismael Ceballos
 */
public interface ObjectiveService {

    Objective findById(int id);

    List<Objective> findAll();

    boolean existsById(int id);

    boolean ObjectiveAchieved(PlayerGame playerInTurn);

    boolean verifyTerritorialObjective(PlayerGame playerGame, Objective obj);
}
