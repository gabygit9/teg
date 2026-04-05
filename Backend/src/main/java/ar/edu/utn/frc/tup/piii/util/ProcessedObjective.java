package ar.edu.utn.frc.tup.piii.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Representa un objetivo descompuesto y estructurado, listo para que la lógica del juego o los bots lo utilicen.
 * Puede incluir conquista de continentes completos, cantidades de países por continente, países individuales
 * o la eliminación de un jugador según su color.
 *
 * @author GabrielaCamacho
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedObjective {
    private ObjectiveType type;

    /** Continentes que deben ser completamente conquistados */
    private List<String> totalContinents;

    /** Requiere conquistar una cierta cantidad de países dentro de un continente específico */
    private Map<String, Integer> countriesPerContinent;

    /** Lista de nombres de países individuales que deben ser conquistados (si aplica) */
    private List<String> singleCountries;

    /** Color del ejército/jugador a eliminar (para objetivos de eliminación) */
    private String objectiveColor;

    /** Para objetivos de ocupación general: cantidad total de países a ocupar */
    private Integer quantityGlobalCountry;

    public ProcessedObjective(ObjectiveType type){
        this.type = type;
        this.totalContinents = new ArrayList<>();
    }
}
