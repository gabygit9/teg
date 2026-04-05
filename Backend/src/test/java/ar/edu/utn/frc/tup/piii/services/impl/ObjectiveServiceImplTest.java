package ar.edu.utn.frc.tup.piii.services.impl;

import ar.edu.utn.frc.tup.piii.model.entities.Game;
import ar.edu.utn.frc.tup.piii.model.entities.Objective;
import ar.edu.utn.frc.tup.piii.model.entities.PlayerGame;
import ar.edu.utn.frc.tup.piii.model.entities.CountryGame;
import ar.edu.utn.frc.tup.piii.model.repository.ObjectiveRepository;
import ar.edu.utn.frc.tup.piii.services.interfaces.ContinentService;
import ar.edu.utn.frc.tup.piii.services.interfaces.PlayerService;
import ar.edu.utn.frc.tup.piii.services.interfaces.CountryGameService;
import ar.edu.utn.frc.tup.piii.util.AnalizeObjective;
import ar.edu.utn.frc.tup.piii.util.ProcessedObjective;
import ar.edu.utn.frc.tup.piii.util.ObjectiveType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ObjectiveServiceImplTest {

    @Mock
    private ObjectiveRepository objectiveRepository;

    @Mock
    private CountryGameService countryGameService;

    @Mock
    private PlayerService playerService;

    @Mock
    private ContinentService continentService;

    @Spy
    @InjectMocks
    private ObjectiveServiceImpl objetivoServiceImpl;


    @Test
    void test_find_by_id() {
        Objective objective = new Objective();
        objective.setId(1);
        when(objectiveRepository.findById(1)).thenReturn(Optional.of(objective));

        Objective found = objetivoServiceImpl.findById(1);

        assertNotNull(found);
        assertEquals(1, found.getId());
        verify(objectiveRepository).findById(1);
    }

    @Test
    void findIdDoesntExistsTest() {
        when(objectiveRepository.findById(99)).thenReturn(Optional.empty());

        Objective result = objetivoServiceImpl.findById(99);

        assertNull(result);
        verify(objectiveRepository).findById(99);
    }

    @Test
    void findAllNo16Test() {
        Objective o1 = new Objective(); o1.setId(1);
        Objective o2 = new Objective(); o2.setId(16);
        Objective o3 = new Objective(); o3.setId(3);

        when(objectiveRepository.findAll()).thenReturn(Arrays.asList(o1, o2, o3));

        List<Objective> result = objetivoServiceImpl.findAll();

        assertEquals(2, result.size());
        assertFalse(result.stream().anyMatch(o -> o.getId() == 16));
        verify(objectiveRepository).findAll();
    }

    @Test
    void existByIdTest() {
        when(objectiveRepository.existsById(1)).thenReturn(true);

        assertTrue(objetivoServiceImpl.existsById(1));
        verify(objectiveRepository).existsById(1);
    }

    @Test
    void existsByIdDoesntExistsTest() {
        when(objectiveRepository.existsById(999)).thenReturn(false);

        assertFalse(objetivoServiceImpl.existsById(999));
        verify(objectiveRepository).existsById(999);
    }


    @Test
    void testObjectiveAchieved_Common_Achieved() {
        PlayerGame playerGame = mock(PlayerGame.class);
        Objective objective = new Objective();
        objective.setId(16);  // ID asociado al objetivo común
        objective.setDescription("Controlar 30 países");

        when(playerGame.getSecretObjective()).thenReturn(objective);

        ProcessedObjective processed = mock(ProcessedObjective.class);
        when(processed.getType()).thenReturn(ObjectiveType.COMMON_OBJECTIVE);
        when(processed.getQuantityGlobalCountry()).thenReturn(30);

        List<CountryGame> country = new ArrayList<>();
        for (int i = 0; i < 30; i++) country.add(new CountryGame());
        when(countryGameService.findByPlayerGame(playerGame)).thenReturn(country);

        try (MockedStatic<AnalizeObjective> mockAnalyzer = mockStatic(AnalizeObjective.class)) {
            mockAnalyzer.when(() -> AnalizeObjective.detectType(16)).thenReturn(ObjectiveType.COMMON_OBJECTIVE);
            mockAnalyzer.when(() -> AnalizeObjective.analizeObjective(objective)).thenReturn(processed);


            boolean result = objetivoServiceImpl.ObjectiveAchieved(playerGame);
            assertTrue(result);
        }
    }

    @Test
    void testAchievedObjective_Color_Achieved() {
        PlayerGame playerGame = mock(PlayerGame.class);
        Game game = mock(Game.class);

        Objective objective = new Objective();
        objective.setId(25);
        objective.setDescription("Eliminar ejército rojo");

        when(playerGame.getSecretObjective()).thenReturn(objective);
        when(playerGame.getGame()).thenReturn(game);
        when(game.getId()).thenReturn(1);

        ProcessedObjective processed = mock(ProcessedObjective.class);
        when(processed.getType()).thenReturn(ObjectiveType.ARMY_COLOR);

        when(playerService.wasEliminatedColor("eliminar ejército rojo", 1)).thenReturn(true);


        try (MockedStatic<AnalizeObjective> mockAnalyzer = mockStatic(AnalizeObjective.class)) {
            mockAnalyzer.when(() -> AnalizeObjective.detectType(25))
                    .thenReturn(ObjectiveType.ARMY_COLOR);
            mockAnalyzer.when(() -> AnalizeObjective.analizeObjective(objective))
                    .thenReturn(processed);

            boolean result = objetivoServiceImpl.ObjectiveAchieved(playerGame);
            assertTrue(result);
        }
    }

    @Test
    void testAchievedObjective_Territorial_Achieved() {
        PlayerGame playerGame = mock(PlayerGame.class);
        Objective objective = new Objective();
        objective.setId(1);
        objective.setDescription("Controlar América del Sur y 2 países de Asia");

        when(playerGame.getSecretObjective()).thenReturn(objective);

        ProcessedObjective processed = mock(ProcessedObjective.class);
        when(processed.getType()).thenReturn(ObjectiveType.CONTINENT_AND_COUNTRIES);
        when(processed.getTotalContinents()).thenReturn(List.of("South America"));
        when(processed.getCountriesPerContinent()).thenReturn(Map.of("Asia", 2));
        when(processed.getSingleCountries()).thenReturn(null);

        try (MockedStatic<AnalizeObjective> mockAnalyzer = mockStatic(AnalizeObjective.class)) {
            mockAnalyzer.when(() -> AnalizeObjective.detectType(1))
                    .thenReturn(ObjectiveType.CONTINENT_AND_COUNTRIES);
            mockAnalyzer.when(() -> AnalizeObjective.analizeObjective(objective))
                    .thenReturn(processed);

            when(continentService.continentControlled(playerGame, "South America")).thenReturn(true);
            when(continentService.controlNCountriesOfTheContinent(playerGame, "Asia", 2)).thenReturn(true);

            boolean result = objetivoServiceImpl.ObjectiveAchieved(playerGame);
            assertTrue(result);
        }
    }

    @Test
    void verifyObjectiveCommon_ObjectiveAchieved() {

        PlayerGame playerGame = new PlayerGame();
        ProcessedObjective processed = new ProcessedObjective(ObjectiveType.COMMON_OBJECTIVE);
        processed.setQuantityGlobalCountry(3);

        List<CountryGame> countries = Arrays.asList(
                new CountryGame(), new CountryGame(), new CountryGame(), new CountryGame()
        );

        when(countryGameService.findByPlayerGame(playerGame)).thenReturn(countries);

        boolean result = objetivoServiceImpl.verifyObjectiveCommon(playerGame, processed);

        assertTrue(result);
        verify(countryGameService).findByPlayerGame(playerGame);
    }

    @Test
    void verifyObjectiveCommon_ObjectiveNonAchieved() {
        PlayerGame playerGame = new PlayerGame();
        ProcessedObjective processed = new ProcessedObjective(ObjectiveType.COMMON_OBJECTIVE);
        processed.setQuantityGlobalCountry(5);

        List<CountryGame> countries = Arrays.asList(
                new CountryGame(), new CountryGame()
        );

        when(countryGameService.findByPlayerGame(playerGame)).thenReturn(countries);

        boolean result = objetivoServiceImpl.verifyObjectiveCommon(playerGame, processed);

        assertFalse(result);
        verify(countryGameService).findByPlayerGame(playerGame);
    }

    @Test
    void verifyTerritorial_Objective_BordersEachOther_Achieve() {

        PlayerGame playerGame = new PlayerGame();
        Objective objective = new Objective();
        objective.setId(1);

        ProcessedObjective processed = new ProcessedObjective(ObjectiveType.CONTINENT_AND_COUNTRIES);
        processed.setSingleCountries(List.of("BORDERING_EACH_OTHER"));
        processed.setCountriesPerContinent(new HashMap<>());
        processed.setQuantityGlobalCountry(4);

        try (MockedStatic<AnalizeObjective> mockAnalyzer = mockStatic(AnalizeObjective.class)) {
            mockAnalyzer.when(() -> AnalizeObjective.analizeObjective(objective))
                    .thenReturn(processed);

            when(countryGameService.hasNBorderCountriesEachOther(playerGame, 4)).thenReturn(true);

            boolean result = objetivoServiceImpl.verifyTerritorialObjective(playerGame, objective);

            assertTrue(result);
            verify(countryGameService).hasNBorderCountriesEachOther(playerGame, 4);
        }
    }

    @Test
    void verifyTerritorial_Objective_BordersEachOther_NonAchieve() {
        PlayerGame playerGame = new PlayerGame();
        Objective objective = new Objective();
        objective.setId(2);

        ProcessedObjective processed = new ProcessedObjective(ObjectiveType.CONTINENT_AND_COUNTRIES);
        processed.setSingleCountries(List.of("BORDERING_EACH_OTHER"));
        processed.setCountriesPerContinent(new HashMap<>());
        processed.setQuantityGlobalCountry(4);

        try (MockedStatic<AnalizeObjective> mockAnalyzer = mockStatic(AnalizeObjective.class)) {
            mockAnalyzer.when(() -> AnalizeObjective.analizeObjective(objective))
                    .thenReturn(processed);

            when(countryGameService.hasNBorderCountriesEachOther(playerGame, 4)).thenReturn(false);

            boolean result = objetivoServiceImpl.verifyTerritorialObjective(playerGame, objective);

            assertFalse(result);
            verify(countryGameService).hasNBorderCountriesEachOther(playerGame, 4);
        }
    }

}
