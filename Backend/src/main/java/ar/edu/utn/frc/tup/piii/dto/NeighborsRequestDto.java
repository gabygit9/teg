package ar.edu.utn.frc.tup.piii.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NeighborsRequestDto {
    private int countryId;
    private int playerGameId;
    private int gameId;
}
