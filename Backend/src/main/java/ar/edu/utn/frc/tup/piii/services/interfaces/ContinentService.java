package ar.edu.utn.frc.tup.piii.services.interfaces;

import ar.edu.utn.frc.tup.piii.model.entities.Continent;
import ar.edu.utn.frc.tup.piii.model.entities.Country;
import ar.edu.utn.frc.tup.piii.model.entities.PlayerGame;

import java.util.List;

/**
 * Interfaz que define los métodos para consultar y gestionar los continentes del juego TEG.
 *
 * Un continente está compuesto por varios países y otorga un bonus de ejércitos
 * cuando un jugador controla todos los países que lo conforman.
 *
 * @author GabrielaCamacho
 * @version 1.0
 */
public interface ContinentService {

    /**
     * Obtiene todos los continentes disponibles en el juego.
     *
     * @return Lista de todos los continentes
     */
    List<Continent> getAll();

    /**
     * Obtiene un continente específico por su ID.
     *
     * @param id Identificador único del continente
     * @return El continente correspondiente o null si no se encuentra
     */
    Continent getById(int id);

    /**
     * Obtiene todos los países que pertenecen a un continente específico.
     *
     * @param continentId ID del continente
     * @return Lista de países que pertenecen al continente
     */
    List<Country> getCountriesByContinent(int continentId);

    /**
     * Calcula y retorna la cantidad de ejércitos bonus que otorga un continente.
     * Este bonus se otorga cuando un jugador controla todos los países del continente.
     *
     * @param continentId ID del continente
     * @return Cantidad de ejércitos bonus, o 0 si el continente no existe
     */
    int calculateArmyBonus(int continentId);

    /**
     * Verifica si un jugador controla completamente un continente.
     * Un continente está completo cuando el jugador posee todos los países que lo conforman.
     *
     * @param continentId ID del continente a verificar
     * @param playerId ID del jugador
     * @param gameId ID de la partida actual
     * @return true si el jugador controla todo el continente, false en caso contrario
     */
    boolean isCompleteContinent(int continentId, int playerId, int gameId);

    /**
     * Obtiene la lista de continentes que un jugador controla completamente en una partida.
     * Útil para calcular el total de bonus de ejércitos al inicio de cada turno.
     *
     * @param playerId ID del jugador
     * @param gameId ID de la partida actual
     * @return Lista de continentes controlados completamente por el jugador
     */
    List<Continent> getContinentsControlledByPlayer(int playerId, int gameId);

    /**
     * Calcula el total de ejércitos bonus que recibe un jugador por controlar continentes completos.
     *
     * @param playerId ID del jugador
     * @param gameId ID de la partida actual
     * @return Total de ejércitos bonus por continentes controlados
     */
    int calculateTotalBonusPlayer(int playerId, int gameId);


    Country findCountryById(int id);

    boolean continentControlled(PlayerGame playerGame, String continent);

    boolean controlNCountriesOfTheContinent(PlayerGame playerGame, String continent, int quantity);
}