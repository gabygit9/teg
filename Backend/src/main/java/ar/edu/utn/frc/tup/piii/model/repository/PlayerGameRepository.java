package ar.edu.utn.frc.tup.piii.model.repository;

import ar.edu.utn.frc.tup.piii.model.entities.PlayerGame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la relación jugador-partida
 * {@code @author:} GabrielaCamacho
 */
@Repository
public interface PlayerGameRepository extends JpaRepository<PlayerGame, Integer> {

    List<PlayerGame> findByGame_Id(int id);

    void deleteByGameId(int gameId);

    boolean existsByGame_IdAndPlayer_Id(int gameId, int playerId);

    Optional<PlayerGame> findByGame_IdAndIsTurnIsTrue(int gameId);

    @Query("SELECT jp FROM PlayerGame jp WHERE jp.game.id = :gameId AND jp.id = :playerGameId")
    Optional<PlayerGame> findByIdAndGameId(
            @Param("gameId") int gameId,
            @Param("playerGameId") int playerGameId);

    Optional<PlayerGame> findByGame_IdAndColor_Name(int id, String colorObjective);
}
