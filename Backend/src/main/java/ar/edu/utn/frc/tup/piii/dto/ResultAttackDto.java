package ar.edu.utn.frc.tup.piii.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ResultAttackDto implements Serializable {
    @JsonProperty("attackerDice")
    private List<Integer> attackerDice;
    @JsonProperty("deffenderDice")
    private List<Integer> deffenderDice;
    @JsonProperty("wasConquest")
    boolean wasConquest;
}
