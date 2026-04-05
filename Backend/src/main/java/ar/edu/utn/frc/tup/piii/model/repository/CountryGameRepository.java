package ar.edu.utn.frc.tup.piii.model.repository;

import ar.edu.utn.frc.tup.piii.model.entities.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para el estado de cada país dentro de una partida.
 * {@code @author:} GabrielaCamacho
 */
@Repository
public interface CountryGameRepository extends JpaRepository<CountryGame, CountryGameId> {
    List<CountryGame> findByPlayerGame(PlayerGame playerGame);
    List<CountryGame> findByGame(Game game);

    @Query(" " +
            "SELECT pp FROM CountryGame pp " +
            "JOIN FETCH pp.playerGame " +
            "JOIN FETCH pp.country " +
            "JOIN FETCH pp.game " +
            "WHERE pp.id.countryId = :countryId AND pp.id.gameId = :gameId")
    Optional<CountryGame> findByIdWithAll(@Param("countryId") int countryId, @Param("gameId") int gameId);

    @Query(" " +
            "SELECT pp FROM CountryGame pp " +
            "JOIN FETCH pp.playerGame " +
            "JOIN FETCH pp.country " +
            "JOIN FETCH pp.game " +
            "WHERE pp.id.countryId = :countryId AND pp.id.gameId = :gameId")
    Optional<CountryGame> findByIdSimple(@Param("countryId") int countryId, @Param("gameId") int gameId);

    @Modifying
    @Query("DELETE FROM CountryGame p WHERE p.id.gameId = :gameId")
    void deleteByGameId(@Param("gameId") int gameId);


    @Query("SELECT p FROM CountryGame p WHERE p.country = :pais AND p.game.id = :gameId")
    CountryGame findByCountryAndGameId(@Param("country") Country country, @Param("gameId") int gameId);

    List<CountryGame> findByGame_IdAndPlayerGame_Id(int gameId, int playerId);
}

