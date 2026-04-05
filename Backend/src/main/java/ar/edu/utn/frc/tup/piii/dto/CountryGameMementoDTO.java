package ar.edu.utn.frc.tup.piii.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CountryGameMementoDTO {
    private int countryId;
    private String countryName;
    private String continent;
    private int playerGameId;
    private String playerName;
    private String playerColor;
    private int availableArmies;
}
