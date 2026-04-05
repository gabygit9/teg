package ar.edu.utn.frc.tup.piii.model.repository;

import ar.edu.utn.frc.tup.piii.model.entities.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para los mensajes enviados en la partida.
 * {@code @author:} GabrielaCamacho
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {
    List<Message> findByGame_Id(int id);

    void deleteByGameId(int gameId);

    @Query(value = "SELECT * FROM Messages WHERE message_game_id = :gameId ORDER BY date_time_message ASC", nativeQuery = true)
    List<Message> findByGameIdOrderByHourAsc(int gameId);
}