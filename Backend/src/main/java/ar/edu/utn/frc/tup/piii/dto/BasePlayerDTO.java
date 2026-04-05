package ar.edu.utn.frc.tup.piii.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) que representa la información básica de un jugador.
 * Utilizado para transferir datos entre las capas de la aplicación y en las
 * comunicaciones de la API REST.
 *
 * @author GabrielaCamacho
 * @version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BasePlayerDTO {
    private int id;
    private String playerName;
    private int availableArmies;
}
