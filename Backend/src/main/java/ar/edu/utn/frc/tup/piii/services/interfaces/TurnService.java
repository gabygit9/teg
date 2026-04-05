package ar.edu.utn.frc.tup.piii.services.interfaces;

import ar.edu.utn.frc.tup.piii.model.entities.PlayerGame;
import ar.edu.utn.frc.tup.piii.model.entities.CountryGameId;
import ar.edu.utn.frc.tup.piii.model.entities.Game;
import ar.edu.utn.frc.tup.piii.model.entities.Turn;

import java.util.List;

/**
 * Interfaz que define las operaciones relacionadas con la gestión de turnos en una partida.
 * Un turno está compuesto por distintas fases del juego: incorporación, ataque y reagrupación.
 *
 * @author Ismael Ceballos
 */
public interface TurnService {

    boolean save(Turn turn);

    Turn findById(int id);

    List<Turn> findAll();

    /**
     * Inicia un nuevo turno para el jugador actual.
     * @param playerGame El jugador que debe comenzar el turno.
     * @param game La partida en curso.
     */
    void startTurn(PlayerGame playerGame, Game game);

    /**
     * Avanza a la siguiente fase del turno actual (incorporación → ataque → reagrupación).
     * @param turn Turno actual.
     */
    void movePhase(Turn turn);

    /**
     * Retorna las acciones disponibles en la fase actual del turno.
     * @param turn Turno en curso.
     * @return Lista de acciones permitidas.
     */
    List<String> getAvailableActions(Turn turn);

    /**
     * Finaliza el turno actual y pasa el control al siguiente jugador.
     * @param turn Turno a finalizar.
     */
    void finishTurn(Turn turn);
    void finishTurnRound(int id, Turn turn);

    void putArmy(int gameId, int countryId, int quantity);

    Turn getPlayerGameId(int playerGameId);

    void moveArmies(CountryGameId idOrigin, CountryGameId idDestination, int troopsToMove);

    void startFirstTurnOfTheGame(Game game);

    int getPlayerInTurn(int gameId);

}
