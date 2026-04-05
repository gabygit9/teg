package ar.edu.utn.frc.tup.piii.mappers.mementoMappers;

import ar.edu.utn.frc.tup.piii.dto.mementosDTOs.TurnMementoDTO;
import ar.edu.utn.frc.tup.piii.model.entities.Turn;

public class TurnMementoMapper {

    public static TurnMementoDTO toDTO(Turn turn) {
        return new TurnMementoDTO(
                turn.getId(),
                turn.getPlayerGame().getId(),
                turn.getPlayerGame().getPlayer().getName(),
                turn.getCurrentPhase(),
                turn.getInitialStartDate(),
                turn.getMaxDuration(),
                turn.getAvailableArmies(),
                turn.getFinished()
        );
    }
}
