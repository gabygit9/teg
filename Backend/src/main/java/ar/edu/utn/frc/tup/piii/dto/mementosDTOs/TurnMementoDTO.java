package ar.edu.utn.frc.tup.piii.dto.mementosDTOs;

import ar.edu.utn.frc.tup.piii.model.enums.TurnPhase;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TurnMementoDTO {
    private int id;
    private int playerGameId;
    private String playerName;
    private TurnPhase currentPhase;
    private LocalDateTime dateStartTurn;
    private int maximunDuration;
    private int availableArmies;
    private boolean finished;
}
