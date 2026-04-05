package ar.edu.utn.frc.tup.piii.services.interfaces;

import ar.edu.utn.frc.tup.piii.dto.BigJsonDTO;
import ar.edu.utn.frc.tup.piii.dto.GameDTO;
import ar.edu.utn.frc.tup.piii.model.entities.*;
import ar.edu.utn.frc.tup.piii.model.enums.StateGameEnum;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Interface que define las operaciones principales sobre la entidad Partida.
 * Representa la capa de servicio que gestiona la lógica relacionada ah las partidas del juego.
 *
 * @author Ismael Ceballos
 */
public interface GameService {

    boolean save(Game game);

    Game findById(int id);

    List<Game> findAll();

    void update(Game game);

    /**
     * Inicia una partida desde el estado "Preparación".
     * Asigna países, jugadores, objetivos, etc.
     *
     * @param game ID de la partida
     * @return true si se inició correctamente
     */
    Game startGame(Game game);

    /**
     * Finaliza una partida estableciendo su estado como "Finalizada".
     *
     * @param gameId ID de la partida
     * @return true si se finalizó correctamente
     */
    boolean endGame(int gameId);

    /**
     * Cambia la partida al estado de hostilidades: comienzan los turnos.
     *
     * @param gameId ID de la partida
     */
    void initHostilities(int gameId);

    /**
     * Lógica para tirar los dados en una batalla.
     *
     * @param attackerId ID del jugador atacante
     * @param defenderId ID del jugador defensor
     * @return resultado del dado como objeto o lista
     */
    List<Integer> throwDice(int attackerId, int defenderId, int maxDice);

    /**
     * Registra la conquista de un país por parte de un jugador.
     *
     * @param countryId ID del país conquistado
     * @param player el nuevo jugador propietario
     */
    void conquerCountry(int countryId, PlayerGame player);

    /**
     * Devuelve el tipo de comunicación activa (Fair Play, Vale Tod_o).
     *
     * @param gameId ID de la partida
     * @return descripción del estilo de comunicación
     */
    String communicationStyle(int gameId);

    /**
     * Asigna un objetivo común a una partida.
     * Este objetivo es visible para todos los jugadores y se guarda en la partida como referencia.
     *
     * @param gameId ID de la partida en la que se asignará el objetivo.
     *
     * @throws NoSuchElementException si la partida no existe.
     */
    void assignCommonObjective(int gameId);

    /**
     * Mueve un número de tropas de un país a otro, siendo países pertenecientes al mismo jugador.
     *
     * @param idOrigin id de País de origen de donde se moverán las tropas.
     * @param idDestination id de País de destino de donde se moverán las tropas.
     * @param troopsToMove entero de la cantidad de tropas.
     */
    void moveArmies(CountryGameId idOrigin, CountryGameId idDestination, int troopsToMove);

    /**
     * Verifica que el jugador haya ganado la partida, tanto por su
     * objetivo secreto como por el objetivo común.
     *
     * @param player Entidad jugador el cual se validará su estado.
     * @param game Entidad partida en la cual se encuentra el jugador.
     */
    boolean verifyVictory(PlayerGame player, Game game);

    /**
     * Anuncia el ataque de un jugador a otro.
     */
    void announceAttack(CountryGameId attackerId, CountryGameId defenderId);

    Game dtoToEntity(GameDTO game);

    GameDTO entityToDto(Game game);

    List<PlayerGame> playersOfAGame(int gameId);

    void assignSecretObjectives(int id);

    void assignOrderTurnByDice(Game game);

    void assignInitialArmies(int id);

    void moveState(int id);

    Optional<StateGameEntity> findStateByDescription(String name);

    List<PlayerGame> getPlayerGame(int gameId);

    void changeState(Game game, StateGameEnum newStateEnum);

    BigJsonDTO getDataGame(int gameId);

    void startFirstTurnOfTheGame(Game game);



}