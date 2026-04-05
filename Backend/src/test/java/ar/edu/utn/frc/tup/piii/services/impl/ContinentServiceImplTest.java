package ar.edu.utn.frc.tup.piii.services.impl;

import ar.edu.utn.frc.tup.piii.model.entities.*;
import ar.edu.utn.frc.tup.piii.model.repository.ContinentRepository;
import ar.edu.utn.frc.tup.piii.model.repository.CountryGameRepository;
import ar.edu.utn.frc.tup.piii.model.repository.CountryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContinentServiceImplTest {

    @Mock
    private ContinentRepository continentRepository;

    @Mock
    private CountryRepository countryRepository;

    @Mock
    private CountryGameRepository countryGameRepository;

    @InjectMocks
    private ContinentServiceImpl continentService;

    private Continent continent1;
    private Continent continent2;
    private Country country1;
    private Country country2;
    private Country country3;
    private Game game;
    private PlayerGame playerGame;
    private CountryGame countryGame1;
    private CountryGame countryGame2;

    @BeforeEach
    void setUp() {
        // Crear continentes
        continent1 = new Continent(1, "South America", 2);
        continent2 = new Continent(2, "Europe", 5);

        // Crear países
        country1 = new Country(1, "Argentine", continent1);
        country2 = new Country(2, "Brazil", continent1);
        country3 = new Country(3, "France", continent2);

        // Crear partida y jugador
        game = new Game();
        game.setId(1);

        playerGame = new PlayerGame();
        playerGame.setId(1);

        // Crear países partida
        countryGame1 = new CountryGame(country1, game, playerGame, 5);
        countryGame2 = new CountryGame(country2, game, playerGame, 3);
    }

    @Test
    void getAllContinents() {
        // Arrange
        List<Continent> expectedContinents = Arrays.asList(continent1, continent2);
        when(continentRepository.findAll()).thenReturn(expectedContinents);

        // Act
        List<Continent> result = continentService.getAll();

        // Assert
        assertEquals(2, result.size());
        assertEquals(expectedContinents, result);
        verify(continentRepository).findAll();
    }

    @Test
    void getById_WhenExists_ShouldReturnContinent() {

        when(continentRepository.findById(1)).thenReturn(Optional.of(continent1));


        Continent result = continentService.getById(1);


        assertNotNull(result);
        assertEquals(continent1, result);
        verify(continentRepository).findById(1);
    }

    @Test
    void getById_WhenDoesntExists_ShouldReturnNull() {

        when(continentRepository.findById(999)).thenReturn(Optional.empty());

        Continent result = continentService.getById(999);


        assertNull(result);
        verify(continentRepository).findById(999);
    }

    @Test
    void getCountriesPerContinent_WhenContinentExists_ShouldReturnCountries() {

        List<Country> allCountries = Arrays.asList(country1, country2, country3);
        when(continentRepository.findById(1)).thenReturn(Optional.of(continent1));
        when(countryRepository.findAll()).thenReturn(allCountries);


        List<Country> result = continentService.getCountriesByContinent(1);


        assertEquals(2, result.size());
        assertTrue(result.contains(country1));
        assertTrue(result.contains(country2));
        assertFalse(result.contains(country3));
        verify(continentRepository).findById(1);
        verify(countryRepository).findAll();
    }

    @Test
    void getCountriesByContinentDoesntExists_ShouldReturnEmptyList() {

        when(continentRepository.findById(999)).thenReturn(Optional.empty());


        List<Country> result = continentService.getCountriesByContinent(999);


        assertTrue(result.isEmpty());
        verify(continentRepository).findById(999);
        verify(countryRepository, never()).findAll();
    }

    @Test
    void calculateArmyBonusArmy_WhenContinentExists_ShouldReturnBonus() {

        when(continentRepository.findById(1)).thenReturn(Optional.of(continent1));


        int result = continentService.calculateArmyBonus(1);


        assertEquals(2, result);
        verify(continentRepository).findById(1);
    }

    @Test
    void calculateArmyBonus_WhenContinentDoesntExists_ShouldReturnZero() {

        when(continentRepository.findById(999)).thenReturn(Optional.empty());


        int result = continentService.calculateArmyBonus(999);


        assertEquals(0, result);
        verify(continentRepository).findById(999);
    }

    @Test
    void isCompleteContinent_WhenPlayerControlAllCountries_ShouldReturnTrue() {

        List<Country> countriesContinent = Arrays.asList(country1, country2);
        List<CountryGame> playerCountry = Arrays.asList(countryGame1, countryGame2);

        when(continentRepository.findById(1)).thenReturn(Optional.of(continent1));
        when(countryRepository.findAll()).thenReturn(Arrays.asList(country1, country2, country3));
        when(countryGameRepository.findByGame_IdAndPlayerGame_Id(1, 1))
                .thenReturn(playerCountry);


        boolean result = continentService.isCompleteContinent(1, 1, 1);


        assertTrue(result);
        verify(continentRepository).findById(1);
        verify(countryRepository).findAll();
        verify(countryGameRepository).findByGame_IdAndPlayerGame_Id(1, 1);
    }

    @Test
    void isCompleteContinent_WhenPlayerDoesntControlAllCountries_ShouldReturnFalse() {

        List<CountryGame> playerCountries = Arrays.asList(countryGame1); // Solo controla un país

        when(continentRepository.findById(1)).thenReturn(Optional.of(continent1));
        when(countryRepository.findAll()).thenReturn(Arrays.asList(country1, country2, country3));
        when(countryGameRepository.findByGame_IdAndPlayerGame_Id(1, 1))
                .thenReturn(playerCountries);


        boolean result = continentService.isCompleteContinent(1, 1, 1);


        assertFalse(result);
        verify(continentRepository).findById(1);
        verify(countryRepository).findAll();
        verify(countryGameRepository).findByGame_IdAndPlayerGame_Id(1, 1);
    }

    @Test
    void isContinentComplete_WhenThereAreNotCountriesInContinent_ShouldReturnFalse() {

        when(continentRepository.findById(1)).thenReturn(Optional.of(continent1));
        when(countryRepository.findAll()).thenReturn(Arrays.asList(country3)); // Solo países de otros continentes


        boolean result = continentService.isCompleteContinent(1, 1, 1);


        assertFalse(result);
        verify(continentRepository).findById(1);
        verify(countryRepository).findAll();
        verify(countryGameRepository, never()).findByGame_IdAndPlayerGame_Id(anyInt(), anyInt());
    }

    @Test
    void getContinentsControlledByPlayer_ShouldReturnContinentsComplete() {

        List<Continent> allContinents = Arrays.asList(continent1, continent2);
        when(continentRepository.findAll()).thenReturn(allContinents);

        // Mock para continente1 - controlado completamente
        when(continentRepository.findById(1)).thenReturn(Optional.of(continent1));
        when(countryRepository.findAll()).thenReturn(Arrays.asList(country1, country2, country3));
        when(countryGameRepository.findByGame_IdAndPlayerGame_Id(1, 1))
                .thenReturn(Arrays.asList(countryGame1, countryGame2));

        // Mock para continente2 - no controlado completamente
        when(continentRepository.findById(2)).thenReturn(Optional.of(continent2));


        List<Continent> result = continentService.getContinentsControlledByPlayer(1, 1);


        assertEquals(1, result.size());
        assertEquals(continent1, result.get(0));
        verify(continentRepository, atLeastOnce()).findAll();
    }

    @Test
    void calculateTotalBonusPlayer_ShouldReturnSumOfBonusOfContinentsControlled() {
        List<Continent> allContinents = Arrays.asList(continent1, continent2);

        when(continentRepository.findAll()).thenReturn(allContinents);

        // Mock para que ambos continentes estén controlados
        when(continentRepository.findById(1)).thenReturn(Optional.of(continent1));
        when(continentRepository.findById(2)).thenReturn(Optional.of(continent2));
        when(countryRepository.findAll()).thenReturn(Arrays.asList(country1, country2, country3));
        when(countryGameRepository.findByGame_IdAndPlayerGame_Id(1, 1))
                .thenReturn(Arrays.asList(countryGame1, countryGame2));


        int result = continentService.calculateTotalBonusPlayer(1, 1);


        assertEquals(2, result); // 2 + 5 = 7
        verify(continentRepository, atLeastOnce()).findAll();
    }

    @Test
    void calculateTotalBonusPlayer_WhenDoesntControlAnyContinent_ShouldReturnZero() {
        // Arrange
        when(continentRepository.findAll()).thenReturn(Arrays.asList(continent1, continent2));
        when(continentRepository.findById(anyInt())).thenReturn(Optional.of(continent1));
        when(countryRepository.findAll()).thenReturn(Arrays.asList(country1, country2, country3));
        when(countryGameRepository.findByGame_IdAndPlayerGame_Id(1, 1))
                .thenReturn(List.of()); // No controla ningún país


        int result = continentService.calculateTotalBonusPlayer(1, 1);


        assertEquals(0, result);
        verify(continentRepository, atLeastOnce()).findAll();
    }

    @Test
    void findCountryById_WhenExists_ShouldReturnCountry() {

        when(countryRepository.findById(1)).thenReturn(Optional.of(country1));


        Country result = continentService.findCountryById(1);


        assertNotNull(result);
        assertEquals(country1, result);
        verify(countryRepository).findById(1);
    }

    @Test
    void findCountryById_WhenDoesntExists_ShouldReturnNull() {

        when(countryRepository.findById(999)).thenReturn(Optional.empty());


        Country result = continentService.findCountryById(999);


        assertNull(result);
        verify(countryRepository).findById(999);
    }
}