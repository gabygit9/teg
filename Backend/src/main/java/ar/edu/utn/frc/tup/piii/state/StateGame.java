package ar.edu.utn.frc.tup.piii.state;

import ar.edu.utn.frc.tup.piii.model.entities.StateGameEntity;
import ar.edu.utn.frc.tup.piii.model.entities.Game;

public interface StateGame {

    /**
     *
     * Este metodo debe coordinar lo que debe ocurrir en esta fase:
     * - Preparación: asignar países, objetivos, etc.
     * - Rondas: asignar ejércitos, controlar turnos.
     * - Hostilidades: controlar el flujo de ataques y reagrupación.
     * - Finalización: verificar condiciones de victoria.
     */
    void executeTurn(Game game);
    /**
     * Este metodo define cuál es el próximo estado después de este.
     * Debe actualizar el estado en el contexto y, si corresponde, en la entidad Partida.
     */
    StateGameEntity moveState(Game game); //Devuelve el proximo estado

}
