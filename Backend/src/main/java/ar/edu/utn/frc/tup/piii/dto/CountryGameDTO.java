package ar.edu.utn.frc.tup.piii.dto;

import lombok.*;

/**
 * DTO que representa un país dentro de una partida.
 * Tiene info relevante del país, sus propietarios y cantidad de ejércitos.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CountryGameDTO {
    private int countryId;
    private int gameId;
    private String countryName;
    private String continent;
    private int availableArmies;

    private int playerId;
    private String playerName;
    private String color;
}
