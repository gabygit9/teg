package ar.edu.utn.frc.tup.piii.services.impl;

import ar.edu.utn.frc.tup.piii.model.entities.*;
import ar.edu.utn.frc.tup.piii.model.repository.ContinentRepository;
import ar.edu.utn.frc.tup.piii.model.repository.CountryGameRepository;
import ar.edu.utn.frc.tup.piii.model.repository.CountryRepository;
import ar.edu.utn.frc.tup.piii.services.interfaces.ContinentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContinentServiceImpl implements ContinentService {

    private final ContinentRepository continentRepository;

    @Override
    public boolean controlNCountriesOfTheContinent(PlayerGame playerGame, String continent, int quantity) {
        int gameId = playerGame.getGame().getId();
        int playerId = playerGame.getId();

        List<CountryGame> playerCountries = countryGameRepository
                .findByGame_IdAndPlayerGame_Id(gameId, playerId);

        // filtrar por continente y contar cuántos controla en ese continente
        long quantityControlled = playerCountries.stream()
                .filter(pp -> pp.getCountry().getContinent().getName().equalsIgnoreCase(continent))
                .count();

        return quantityControlled >= quantity;
    }

    private final CountryRepository countryRepository;
    private final CountryGameRepository countryGameRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Continent> getAll() {
        return continentRepository.findAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Continent getById(int id) {
        return continentRepository.findById(id).orElse(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Country> getCountriesByContinent(int continentId) {
        Continent continent = getById(continentId);
        if (continent == null) {
            return List.of();
        }

        return countryRepository.findAll().stream()
                .filter(country -> country.getContinent().getId() == continentId)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int calculateArmyBonus(int continentId) {
        Continent continent = getById(continentId);
        return continent != null ? continent.getArmyBonus() : 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCompleteContinent(int continentId, int playerId, int gameId) {
        // Obtener los países del continente
        List<Country> countriesOfContinent = getCountriesByContinent(continentId);

        // Si no hay países en el continente, no puede estar completo
        if (countriesOfContinent.isEmpty()) {
            return false;
        }

        // Obtener todos los países que este jugador controla en la partida
        List<CountryGame> playerCountries = countryGameRepository.findByGame_IdAndPlayerGame_Id(gameId, playerId);

        // Extraer los IDs de los países que controla
        Set<Integer> playerCountriesIds = playerCountries.stream()
                .map(pp -> pp.getCountry().getId())
                .collect(Collectors.toSet());

        // Verificar si controla todos los países del continente
        return countriesOfContinent.stream()
                .allMatch(pais -> playerCountriesIds.contains(pais.getId()));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<Continent> getContinentsControlledByPlayer(int playerId, int gameId) {
        return getAll().stream()
                .filter(continent -> isCompleteContinent(continent.getId(), playerId, gameId))
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int calculateTotalBonusPlayer(int playerId, int gameId) {
        List<Continent> controlledContinents = getContinentsControlledByPlayer(playerId, gameId);

        return controlledContinents.stream()
                .mapToInt(Continent::getArmyBonus)
                .sum();
    }

    @Override
    public Country findCountryById(int id) {
        return countryRepository.findById(id).orElse(null);
    }

    @Override
    public boolean continentControlled(PlayerGame playerGame, String continent) {
        int gameId = playerGame.getGame().getId();
        int playerId = playerGame.getId();

        Optional<Continent> continentOpt = continentRepository.findByName(continent);
        if(continentOpt.isEmpty()) {
            throw new IllegalArgumentException("Continente no encontrado: " + continent);
        }

        int continentId = continentOpt.get().getId();
        return isCompleteContinent(continentId, playerId, gameId);
    }


}