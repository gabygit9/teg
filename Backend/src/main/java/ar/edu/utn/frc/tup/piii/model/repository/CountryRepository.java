package ar.edu.utn.frc.tup.piii.model.repository;

import ar.edu.utn.frc.tup.piii.model.entities.Country;
import ar.edu.utn.frc.tup.piii.model.entities.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para países.
 * {@code @author:} GabrielaCamacho
 */
@Repository
public interface CountryRepository extends JpaRepository<Country, Integer> {
    @Query("SELECT p.country FROM CountryGame p WHERE p.game = :game")
    List<Country> findAllByGame(Game game);

    Optional<Country> findByName(String name);

}
