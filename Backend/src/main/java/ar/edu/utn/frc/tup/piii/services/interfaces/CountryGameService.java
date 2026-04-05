package ar.edu.utn.frc.tup.piii.services.interfaces;

import ar.edu.utn.frc.tup.piii.dto.CountryGameDTO;
import ar.edu.utn.frc.tup.piii.model.entities.*;

import java.util.List;

/**
 * Interfaz que define las operaciones para la gestión de la relación entre países y partidas.
 * Este servicio permite registrar, actualizar y consultar el estado de los países en cada partida.
 *
 * @author Ismael Ceballos
 */

public interface CountryGameService {

    boolean save(CountryGame game);

    boolean update(CountryGame game);

    CountryGame findById(int countryId, int gameId);

    List<CountryGame> findAll();

    /**
     * Busca todos los países ocupados por un playerGame.
     */
    List<CountryGame> findByPlayerGame(PlayerGame playerGame);

    /**
     * Retorna todos los países ocupados por un player en una game específica.
     */
    List<CountryGame> findByGameAndPlayerGame(int game, int player);

    /**
     * Retorna todos los países de una partida (sin importar el jugador).
     */
    List<CountryGame> findByGame(Game game);

    /**
     * Incrementa la quantity de ejércitos en un país específico.
     */
    boolean increaseArmies(CountryGameId id, int quantity);
    /**
     * Decrementa la quantity de ejércitos en un país específico.
     */
    boolean reduceArmies(CountryGameId id, int quantity);

    List<CountryGame> findEnemyNeighbors(int id, PlayerGame playerGame, Game game);

    CountryGame[] getBorder(CountryGame current, List<CountryGame> allCountries);

    boolean isBordering(int id, int id1);

    List<CountryGameDTO> getCountriesOfGame(int gameId);

    List<CountryGame> distributeInitialCountries(int gameId);

    CountryGame findByCountryAndGameId(Country country, int gameId);

    boolean checkVictory(PlayerGame playerGame, Game game);

    CountryGameDTO countryGameToDTO(CountryGame pp);

    boolean hasNBorderCountriesEachOther(PlayerGame playerGame, Integer GlobalQuantityCountries);

    boolean controlCountry(PlayerGame playerGame, String country);
}
