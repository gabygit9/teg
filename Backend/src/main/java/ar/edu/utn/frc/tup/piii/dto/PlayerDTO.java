package ar.edu.utn.frc.tup.piii.dto;

import lombok.*;

import java.util.List;

/**
 * DTO que representa a un jugador dentro de una partida.
 * Contiene el estado actual, datos del jugador base, objetivo secreto y sus países ocupados.
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlayerDTO {
    private int playerGameId;
    // Datos del jugador
    private int orderTurn;
    private boolean isTurn;
    private String color;
    private boolean objectiveAchieved;
    private boolean active;


    // Objetivo secreto
    private Integer secretObjectiveId;
    private String secretObjectiveDescription;

    private List<CountryGameDTO> countries;

}
