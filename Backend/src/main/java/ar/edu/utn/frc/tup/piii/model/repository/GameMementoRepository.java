package ar.edu.utn.frc.tup.piii.model.repository;

import ar.edu.utn.frc.tup.piii.model.entities.Game;
import ar.edu.utn.frc.tup.piii.model.entities.GameMemento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface GameMementoRepository extends JpaRepository<GameMemento, Integer> {

    List<GameMemento> findByGameId(int gameId);

    GameMemento findTopByGameIdOrderByDateTimeDesc(int gameId);
    Optional<GameMemento> findTopByGameOrderByDateTimeDesc(Game game);

}

