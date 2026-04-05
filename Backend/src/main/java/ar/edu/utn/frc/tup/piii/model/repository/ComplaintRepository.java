package ar.edu.utn.frc.tup.piii.model.repository;

import ar.edu.utn.frc.tup.piii.model.entities.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para denuncias realizadas por jugadores.
 * {@code @author:} GabrielaCamacho
 */
@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Integer> {
}
