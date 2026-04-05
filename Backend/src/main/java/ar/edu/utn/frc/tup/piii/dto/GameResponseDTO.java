package ar.edu.utn.frc.tup.piii.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameResponseDTO {

    private int gameId;
    private String commonObjective;
    private String communicationType;
    private LocalDateTime dateTime;

}
