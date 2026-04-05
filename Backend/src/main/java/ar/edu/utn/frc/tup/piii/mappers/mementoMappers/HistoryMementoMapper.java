package ar.edu.utn.frc.tup.piii.mappers.mementoMappers;

import ar.edu.utn.frc.tup.piii.dto.mementosDTOs.HistoryMementoDTO;
import ar.edu.utn.frc.tup.piii.model.entities.HistoryEvent;

public class HistoryMementoMapper {

    public static HistoryMementoDTO toDTO(HistoryEvent event) {
        return new HistoryMementoDTO(
                event.getId(),
                event.getDescription(),
                event.getDateTime()
        );
    }
}
