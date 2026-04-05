package ar.edu.utn.frc.tup.piii.mappers.mementoMappers;

import ar.edu.utn.frc.tup.piii.dto.PlayerGameMementoDTO;
import ar.edu.utn.frc.tup.piii.model.entities.PlayerGame;

public class PlayerGameMementoMapper {

    public static PlayerGameMementoDTO toDTO(PlayerGame player) {
        return new PlayerGameMementoDTO(
                player.getId(),
                player.getPlayer().getId(),
                player.getPlayer().getName(),
                player.getColor().getId(),
                player.getColor().getName(),
                player.getSecretObjective().getId(),
                player.getSecretObjective().getDescription(),
                player.isObjectiveAchieved(),
                player.getOrderTurn(),
                player.isTurn(),
                player.isActive()
        );
    }
}
