package ar.edu.utn.frc.tup.piii.mappers;

import ar.edu.utn.frc.tup.piii.dto.ObjectiveDto;
import ar.edu.utn.frc.tup.piii.model.entities.Objective;

public class ObjectiveMapper {
    public static ObjectiveDto toDto(Objective objective) {
        ObjectiveDto dto = new ObjectiveDto();
        dto.setId(objective.getId());
        dto.setDescription(objective.getDescription());
        return dto;
    }

    public static Objective toEntity(ObjectiveDto dto) {
        Objective objective = new Objective();
        objective.setId(dto.getId());
        objective.setDescription(dto.getDescription());
        return objective;
    }
}
