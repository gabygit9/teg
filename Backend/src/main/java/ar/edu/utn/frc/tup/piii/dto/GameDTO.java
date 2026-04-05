package ar.edu.utn.frc.tup.piii.dto;

import ar.edu.utn.frc.tup.piii.model.entities.CommunicationType;
import ar.edu.utn.frc.tup.piii.model.entities.StateGameEntity;
import ar.edu.utn.frc.tup.piii.model.entities.Objective;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameDTO {

    private Integer responseId;

    private int commonObjectiveId;
    private Objective commonObjective;
    private int stateId;
    private StateGameEntity state;
    private CommunicationType CommunicationType;
    private LocalDateTime dateTime;
}
