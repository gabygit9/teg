package ar.edu.utn.frc.tup.piii.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegroupArmyDto {
    private int playerId;
    private int originId;
    private int destinationId;
    private int amount;
}
