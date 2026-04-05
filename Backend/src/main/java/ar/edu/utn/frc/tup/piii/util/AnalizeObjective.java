package ar.edu.utn.frc.tup.piii.util;

import ar.edu.utn.frc.tup.piii.model.entities.Objective;

import java.util.List;
import java.util.Map;

/**
 * Clase utilitaria para analizar y clasificar objetivos en el juego TEG.
 *
 * @author GabrielaCamacho
 */
public class AnalizeObjective {

    /**
     * Detecta el tipo de objetivo según su identificador.
     *
     * @param objectiveId el identificador numérico del objetivo.
     * @return el tipo de {@link ObjectiveType} correspondiente:
     *         <ul>
     *             <li>{@code CONTINENTE_Y_PAISES} para Ids del 1 al 9</li>
     *             <li>{@code COLOR_EJERCITO} para Ids del 10 al 15</li>
     *             <li>{@code DESCONOCIDO} para cualquier otro ID</li>
     *         </ul>
     */
    public static ObjectiveType detectType(int objectiveId){
        return switch (objectiveId){
            case 1,2,3,4,5,6,7,8,9 -> ObjectiveType.CONTINENT_AND_COUNTRIES;
            case 10,11,12,13,14,15 -> ObjectiveType.ARMY_COLOR;
            case 16 -> ObjectiveType.COMMON_OBJECTIVE;
            default -> ObjectiveType.UNKNOWN;
        };
    }


    /**
     * Analiza un objetivo del juego y devuelve su representación estructurada.
     * Esta información puede incluir continentes a conquistar, países por continente, color de objetivo, etc.
     *
     * @param objective el objeto {@link Objective} a analizar.
     * @return un objeto {@link ProcessedObjective} con los datos interpretados
     *         según las reglas del objetivo.
     */
    public static ProcessedObjective analizeObjective(Objective objective){
        int id = objective.getId();

        ObjectiveType tipo = detectType(id);
        ProcessedObjective procesado = new ProcessedObjective(tipo);

        switch(id){
            case 1 -> {
                procesado.setTotalContinents(List.of("África"));
                procesado.setCountriesPerContinent(Map.of(
                        "América del Norte", 5,
                        "Europa", 4
                ));
            }
            case 2 -> {
                procesado.setTotalContinents(List.of("América del Sur"));
                procesado.setCountriesPerContinent(Map.of(
                        "Europa",7
                ));
                procesado.setQuantityGlobalCountry(3);
                procesado.setSingleCountries(List.of("BORDERING_EACH_OTHER"));
            }
            case 3 -> {
                procesado.setTotalContinents(List.of("Asia"));
                procesado.setCountriesPerContinent(Map.of("América del Sur",2));
            }
            case 4 -> {
                procesado.setTotalContinents(List.of("Europa"));
                procesado.setCountriesPerContinent(Map.of(
                        "Asia", 4,
                        "América del Sur", 2
                ));
            }
            case 5 -> {
                procesado.setTotalContinents(List.of("América del Norte"));
                procesado.setCountriesPerContinent(Map.of(
                        "Oceanía", 2,
                        "Asia", 4
                ));
            }
            case 6 -> {
                procesado.setCountriesPerContinent(Map.of(
                        "Oceanía", 2,
                        "África",2,
                        "América del Sur",2,
                        "Europa",3,
                        "América del Norte",4,
                        "Asia",3
                ));
            }
            case 7 -> {
                procesado.setTotalContinents(List.of("Oceanía", "América del Norte"));
                procesado.setCountriesPerContinent(Map.of("Europa",2));
            }
            case 8 -> {
                procesado.setTotalContinents(List.of("América del Sur", "África"));
                procesado.setCountriesPerContinent(Map.of("Asia", 4));
            }
            case 9 -> {
                procesado.setTotalContinents(List.of("Oceanía", "África"));
                procesado.setCountriesPerContinent(Map.of("América del Norte",5));
            }
            case 10 -> {
                procesado.setObjectiveColor("azul");
            }
            case 11 -> {
                procesado.setObjectiveColor("rojo");
            }
            case 12 -> {
                procesado.setObjectiveColor("negro");
            }
            case 13 -> {
                procesado.setObjectiveColor("amarillo");
            }
            case 14 -> {
                procesado.setObjectiveColor("verde");
            }
            case 15 -> {
                procesado.setObjectiveColor("magenta");
            }
            case 16 -> {
                procesado.setQuantityGlobalCountry(30);
            }
            default -> {
                procesado.setTotalContinents(List.of());
                procesado.setCountriesPerContinent(Map.of());
                procesado.setSingleCountries(List.of());
            }
        }
        return procesado;
        }
}
