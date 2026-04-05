package ar.edu.utn.frc.tup.piii.model.repository;

import ar.edu.utn.frc.tup.piii.model.entities.Turn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para turnos de la partida.
 * {@code @author:} GabrielaCamacho
 */
@Repository
public interface TurnRepository extends JpaRepository<Turn, Integer> {
    List<Turn> findByGame_Id(int id);

    void deleteByGameId(int gameId);

    Optional<Turn> findByGame_IdAndFinishedFalse(int gameId);

    Turn findByPlayerGame_IdAndFinishedFalse(int playerGameId);
}
