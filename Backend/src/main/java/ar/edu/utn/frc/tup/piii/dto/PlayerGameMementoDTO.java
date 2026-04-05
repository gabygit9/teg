package ar.edu.utn.frc.tup.piii.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlayerGameMementoDTO {
    private int id;
    private int basePlayerId;
    private String playerName;
    private int colorId;
    private String colorName;
    private int objectiveId;
    private String descriptionObjective;
    private boolean objectiveAchieved;
    private int orderTurn;
    private boolean isTurn;
    private boolean active;
}
