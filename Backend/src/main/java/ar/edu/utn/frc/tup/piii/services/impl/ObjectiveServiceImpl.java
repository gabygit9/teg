package ar.edu.utn.frc.tup.piii.services.impl;

import ar.edu.utn.frc.tup.piii.model.entities.Objective;
import ar.edu.utn.frc.tup.piii.model.entities.PlayerGame;
import ar.edu.utn.frc.tup.piii.model.entities.CountryGame;
import ar.edu.utn.frc.tup.piii.model.repository.ObjectiveRepository;
import ar.edu.utn.frc.tup.piii.services.interfaces.ContinentService;
import ar.edu.utn.frc.tup.piii.services.interfaces.PlayerService;
import ar.edu.utn.frc.tup.piii.services.interfaces.ObjectiveService;
import ar.edu.utn.frc.tup.piii.services.interfaces.CountryGameService;
import ar.edu.utn.frc.tup.piii.util.AnalizeObjective;
import ar.edu.utn.frc.tup.piii.util.ProcessedObjective;
import ar.edu.utn.frc.tup.piii.util.ObjectiveType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Implementación del servicio para gestión de objetivos del juego.
 * Permite asignar objetivos a los jugadores y verificar si los han cumplido,
 * según las reglas de victoria del juego.
 *
 * @author Ismael Ceballos
 *
 * @see Objective
 * @see PlayerGame
 * @see ObjectiveRepository
 */
@Service
@RequiredArgsConstructor
public class ObjectiveServiceImpl implements ObjectiveService {

    private final ObjectiveRepository objectiveRepository;
    private final PlayerService playerService;
    private final ContinentService continentService;
    private final CountryGameService countryGameService;

    @Override
    public boolean ObjectiveAchieved(PlayerGame playerInTurn) {
        Objective objective = playerInTurn.getSecretObjective();
        if (objective == null) return false;

        ObjectiveType type = AnalizeObjective.detectType(objective.getId());
        ProcessedObjective processedObjective = AnalizeObjective.analizeObjective(objective);
        System.out.println("Tipo de objetivo detectado: " + type);
        System.out.println("Descripción completa: " + objective.getDescription());

        return switch (processedObjective.getType()) {
            case CONTINENT_AND_COUNTRIES -> {
                System.out.println("Verificando objetivo de type: " + type);

                yield verifyTerritorialObjective(playerInTurn, objective);
            }
            case ARMY_COLOR -> {
                System.out.println("Verificando objetivo de type: " + type);

                yield verifyObjectiveRemoveColor(playerInTurn, objective);
            }
            case COMMON_OBJECTIVE -> {
                System.out.println("Verificando objetivo de type: " + type);

                yield verifyObjectiveCommon(playerInTurn, processedObjective);
            }
            default -> false;
        };
    }

    boolean verifyObjectiveCommon(PlayerGame playerGame, ProcessedObjective processed) {
        int requiredQuantity = processed.getQuantityGlobalCountry();
        List<CountryGame> countries = countryGameService.findByPlayerGame(playerGame);
        return countries.size() >= requiredQuantity;
    }

    private boolean verifyObjectiveRemoveColor(PlayerGame playerGame, Objective objective) {
        String colorObjective = objective.getDescription().toLowerCase();
        return playerService.wasEliminatedColor(colorObjective, playerGame.getGame().getId());
    }

    @Override
    public boolean verifyTerritorialObjective(PlayerGame playerGame, Objective objective) {
        ProcessedObjective processed = AnalizeObjective.analizeObjective(objective);

        for (String continent : processed.getTotalContinents()) {
            if (!continentService.continentControlled(playerGame, continent)) {
                return false;
            }
        }

        for (Map.Entry<String, Integer> entry : processed.getCountriesPerContinent().entrySet()) {
            String continent = entry.getKey();
            int quantity = entry.getValue();

            if (!continentService.controlNCountriesOfTheContinent(playerGame, continent, quantity)) {
                return false;
            }
        }

        if (processed.getSingleCountries() != null && !processed.getSingleCountries().isEmpty()) {

            if (processed.getSingleCountries().contains("BORDERING_EACH_OTHER")) {
                return countryGameService.hasNBorderCountriesEachOther(playerGame, processed.getQuantityGlobalCountry());
            } else {
                for (String country : processed.getSingleCountries()) {
                    if (!countryGameService.controlCountry(playerGame, country)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    @Override
    public Objective findById(int id) {
        return objectiveRepository.findById(id).orElse(null);
    }

    @Override
    public List<Objective> findAll() {
        List<Objective> res = objectiveRepository.findAll();
        res = res.stream().filter(o -> o.getId() != 16).toList();
        return res;
    }

    @Override
    public boolean existsById(int id) {
        return objectiveRepository.existsById(id);
    }


}
