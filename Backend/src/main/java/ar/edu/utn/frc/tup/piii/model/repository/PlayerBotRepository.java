package ar.edu.utn.frc.tup.piii.model.repository;

import ar.edu.utn.frc.tup.piii.model.entities.BotPlayer;
import ar.edu.utn.frc.tup.piii.model.entities.LevelBot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlayerBotRepository extends JpaRepository<BotPlayer, Integer> {
    List<BotPlayer> findByLevelBot(LevelBot difficulty);
}
