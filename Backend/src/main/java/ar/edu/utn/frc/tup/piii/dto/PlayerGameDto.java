package ar.edu.utn.frc.tup.piii.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PlayerGameDto {
    private int id;
    private String color;
    private ObjectiveDto objective;
    private BasePlayerDTO player;
    @JsonProperty("isHuman")
    private boolean isHuman;
    private boolean deleted;
    private boolean isTurn;
    private int turnId;
    private String currentPhase;
}
