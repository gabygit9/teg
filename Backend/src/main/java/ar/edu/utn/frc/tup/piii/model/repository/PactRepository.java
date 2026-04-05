package ar.edu.utn.frc.tup.piii.model.repository;

import ar.edu.utn.frc.tup.piii.model.entities.Pact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para pactos entre jugadores
 * {@code @author:} GabrielaCamacho
 */
@Repository
public interface PactRepository extends JpaRepository<Pact, Integer> {

    List<Pact> findByGame_Id(int id);

    void deleteByGameId(int gameId);
}
