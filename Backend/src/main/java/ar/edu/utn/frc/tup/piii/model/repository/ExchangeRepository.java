package ar.edu.utn.frc.tup.piii.model.repository;

import ar.edu.utn.frc.tup.piii.model.entities.Exchange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para gestionar operaciones CRUD sobre Canje.
 * {@code @author:} GabrielaCamacho
 */
@Repository
public interface ExchangeRepository extends JpaRepository<Exchange, Integer> {
}
