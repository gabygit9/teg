package ar.edu.utn.frc.tup.piii.dto;


import ar.edu.utn.frc.tup.piii.model.entities.CountryGame;
import ar.edu.utn.frc.tup.piii.model.entities.Game;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RegisterMessageEventDTO {
    private Game game;
    private CountryGame originCountry;
    private CountryGame destinationCountry;
    private Integer amountTroops;
    private String extraDescription;

}
