package ar.edu.utn.frc.tup.piii.util;

import ar.edu.utn.frc.tup.piii.model.entities.CountryGame;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Clase auxiliar para encapsular toda la información relevante de una opción de ataque.
 * Facilita la comparación y ordenamiento de ataques por prioridad estratégica.
 */

@AllArgsConstructor
@Data
public class AttackOption {
    /** País desde el cual se realizaría el ataque */
    final CountryGame originCountry;

    /** País enemigo que sería atacado */
    final CountryGame destinationCountry;

    /** Ratio de ventaja táctica (tropas atacante / tropas defensor) */
    final double troopsAdvantage;

    /** Indica si el país objetivo es estratégicamente importante según el objetivo del jugador */
    final boolean isFromObjective;
}
