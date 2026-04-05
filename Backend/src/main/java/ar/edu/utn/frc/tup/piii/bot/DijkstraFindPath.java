package ar.edu.utn.frc.tup.piii.bot;

import ar.edu.utn.frc.tup.piii.model.entities.CountryGame;
import ar.edu.utn.frc.tup.piii.services.interfaces.CountryGameService;

import java.util.*;

/**
 * Clase utilitaria que implementa el algoritmo de Dijkstra para encontrar el camino más corto
 * entre dos países (nodos) en un grafo de países que representan el mapa del juego TEG.
 *
 * @author GabrielaCamacho
 * @version 1.0
 */
public class DijkstraFindPath {
    /**
     * Encuentra el camino más corto entre dos países utilizando el algoritmo de Dijkstra.
     *
     * @param origin El país de origin.
     * @param destination El país de destination.
     * @param allCountries Lista completa de países en la partida.
     * @param countryGameService Servicio que permite obtener los países limítrofes.
     * @return Una lista con el camino más corto entre el país de origin y el destination.
     *         Si no hay camino, retorna una lista vacía. Si origin y destination son el mismo,
     *         retorna una lista con un solo elemento.
     */
    public static List<CountryGame> findShorterPath(CountryGame origin, CountryGame destination, List<CountryGame> allCountries, CountryGameService countryGameService){
        if(origin == null || destination == null || allCountries == null || allCountries.isEmpty()) return Collections.emptyList();
        if(origin.equals(destination)) return List.of(origin);

        Map<CountryGame, Integer> distances = new HashMap<>();
        Map<CountryGame, CountryGame> previous = new HashMap<>();
        Set<CountryGame> visitedCountries = new HashSet<>();

        //Clave de Dijkstra -> PriorityQueue ordenada x distancia
        PriorityQueue<CountryGame> queue = new PriorityQueue<>(
                Comparator.comparingInt(distances::get)
        );

        //inicializar distances y queue de Dijkstra
        for(CountryGame country : allCountries){
            distances.put(country, Integer.MAX_VALUE);
        }
        distances.put(origin, 0);
        queue.offer(origin);
        
        while(!queue.isEmpty()){
            CountryGame current = queue.poll();

            if(visitedCountries.contains(current)) continue;
            visitedCountries.add(current);

            if(current.equals(destination))break;
            
            for(CountryGame neighbor : countryGameService.getBorder(current, allCountries)){
                if(visitedCountries.contains(neighbor)) continue;
                int newDistance = distances.get(current) + 1;
                
                if(newDistance < distances.get(neighbor)){
                    distances.put(neighbor, newDistance);
                    previous.put(neighbor, current);
                    queue.offer(neighbor);
                }
            }
        }
        return rebuildPath(previous, origin, destination);
    }
    /**
     * Reconstruye el camino más corto desde el país de destination hacia el origin
     * usando el mapa de predecesores generado por Dijkstra.
     *
     * @param previous Mapa que asocia cada país con su predecesor en el camino más corto.
     * @param origin El país de inicio.
     * @param destination El país de destination.
     * @return Lista ordenada de países desde el origin al destination. Vacía si no hay camino.
     */
    private static List<CountryGame> rebuildPath(Map<CountryGame, CountryGame> previous, CountryGame origin, CountryGame destination) {
        LinkedList<CountryGame> path = new LinkedList<>();
        CountryGame current = destination;

        while(current != null && !current.equals(origin)){
            path.addFirst(current);
            current = previous.get(current);
        }
        if(current == null) return List.of(); //No hay path entre los dos paises
        path.addFirst(origin);
        return path;
    }
}
