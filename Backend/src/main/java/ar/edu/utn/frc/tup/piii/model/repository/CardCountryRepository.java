package ar.edu.utn.frc.tup.piii.model.repository;

import ar.edu.utn.frc.tup.piii.model.entities.CardCountry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para tarjetas de país.
 * {@code @author:} GabrielaCamacho
 */
@Repository
public interface CardCountryRepository extends JpaRepository<CardCountry, Integer> {
}
