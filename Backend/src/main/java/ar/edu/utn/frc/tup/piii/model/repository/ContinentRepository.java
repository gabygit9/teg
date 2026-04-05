package ar.edu.utn.frc.tup.piii.model.repository;

import ar.edu.utn.frc.tup.piii.model.entities.Continent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio JPA para los continentes del juego.
 * {@code @author:} GabrielaCamacho
 */
@Repository
public interface ContinentRepository extends JpaRepository<Continent, Integer> {
    Optional<Continent> findByName(String name);
}