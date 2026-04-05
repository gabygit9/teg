package ar.edu.utn.frc.tup.piii.model.repository;

import ar.edu.utn.frc.tup.piii.model.entities.HistoryEvent;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para eventos del historial de la partida.
 * {@code @author:} GabrielaCamacho
 */
@Repository
public interface HistoryRepository extends JpaRepository<HistoryEvent, Integer> {

    List<HistoryEvent> findAllByGameId(int gameId);

    List<HistoryEvent> findByGame_Id(int id);

    @Transactional
    @Modifying
    @Query("DELETE FROM HistoryEvent h WHERE h.game.id = :gameId")
    void deleteByGameId(@Param("gameId") int gameId);

}
