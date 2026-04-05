package ar.edu.utn.frc.tup.piii.util;

import ar.edu.utn.frc.tup.piii.model.entities.Objective;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
class AnalyzeObjectiveTest {

    @Test
    void testDetectTypeContinentAndCountry() {
        for (int i = 1; i <= 9; i++) {
            assertEquals(ObjectiveType.CONTINENT_AND_COUNTRIES, AnalizeObjective.detectType(i));
        }
    }

    @Test
    void testDetectTypeColorArmy() {
        for (int i = 10; i <= 15; i++) {
            assertEquals(ObjectiveType.ARMY_COLOR, AnalizeObjective.detectType(i));
        }
    }

    @Test
    void testDetectTypeUnknown() {
        int[] ids = {-1, -1, 16, 999};
        for (int id : ids) {
            assertNotEquals(ObjectiveType.ARMY_COLOR, AnalizeObjective.detectType(id));
        }
    }

    @Test
    void testAnalyzeObjective_Id1() {
        Objective objective = mock(Objective.class);
        when(objective.getId()).thenReturn(1);

        ProcessedObjective result = AnalizeObjective.analizeObjective(objective);

        assertEquals(ObjectiveType.CONTINENT_AND_COUNTRIES, result.getType());
        assertEquals(List.of("África"), result.getTotalContinents());
        assertEquals(Map.of("North America", 5, "Europe", 4), result.getCountriesPerContinent());
    }

    @Test
    void testAnalyzeObjective_Id2() {
        Objective objective = mock(Objective.class);
        when(objective.getId()).thenReturn(2);
        ProcessedObjective res = AnalizeObjective.analizeObjective(objective);
        assertEquals(List.of("South America"), res.getTotalContinents());
        assertEquals(Map.of("Europe", 7), res.getCountriesPerContinent());
        assertEquals(3, res.getQuantityGlobalCountry());
        assertEquals(List.of("BORDERING_EACH_OTHER"), res.getSingleCountries());
    }

    @Test
    void testAnalyzeObjective_Id3() {
        Objective objective = mock(Objective.class);
        when(objective.getId()).thenReturn(3);
        ProcessedObjective res = AnalizeObjective.analizeObjective(objective);
        assertEquals(List.of("Asia"), res.getTotalContinents());
        assertEquals(Map.of("South America", 2), res.getCountriesPerContinent());
    }

    @Test
    void testAnalyzeObjective_Id4() {
        Objective objective = mock(Objective.class);
        when(objective.getId()).thenReturn(4);
        ProcessedObjective res = AnalizeObjective.analizeObjective(objective);
        assertEquals(List.of("Europe"), res.getTotalContinents());
        assertEquals(Map.of("Asia", 4, "South America", 2), res.getCountriesPerContinent());
    }

    @Test
    void testAnalyzeObjective_Id5() {
        Objective objective = mock(Objective.class);
        when(objective.getId()).thenReturn(5);
        ProcessedObjective res = AnalizeObjective.analizeObjective(objective);
        assertEquals(List.of("North America"), res.getTotalContinents());
        assertEquals(Map.of("Oceanía", 2, "Asia", 4), res.getCountriesPerContinent());
    }

    @Test
    void testAnalyzeObjective_Id6() {
        Objective objective = mock(Objective.class);
        when(objective.getId()).thenReturn(6);
        ProcessedObjective res = AnalizeObjective.analizeObjective(objective);
        assertEquals(Map.of(
                "Oceanía", 2,
                "África", 2,
                "South America", 2,
                "Europe", 3,
                "North America", 4,
                "Asia", 3
        ), res.getCountriesPerContinent());
    }

    @Test
    void testAnalyzeObjective_Id7() {
        Objective objective = mock(Objective.class);
        when(objective.getId()).thenReturn(7);
        ProcessedObjective res = AnalizeObjective.analizeObjective(objective);
        assertEquals(List.of("Oceanía", "North AmericaA"), res.getTotalContinents());
        assertEquals(Map.of("Europe", 2), res.getCountriesPerContinent());
    }

    @Test
    void testAnalyzeObjective_Id8() {
        Objective objective = mock(Objective.class);
        when(objective.getId()).thenReturn(8);
        ProcessedObjective res = AnalizeObjective.analizeObjective(objective);
        assertEquals(List.of("South America", "África"), res.getTotalContinents());
        assertEquals(Map.of("Asia", 4), res.getCountriesPerContinent());
    }

    @Test
    void testAnalyzeObjective_Id9() {
        Objective objective = mock(Objective.class);
        when(objective.getId()).thenReturn(9);
        ProcessedObjective res = AnalizeObjective.analizeObjective(objective);
        assertEquals(List.of("Oceanía", "África"), res.getTotalContinents());
        assertEquals(Map.of("North America", 5), res.getCountriesPerContinent());
    }

    @Test
    void testAnalyzeObjective_Ids10A15_Colors() {
        Map<Integer, String> colors = Map.of(
                10, "blue",
                11, "red",
                12, "black",
                13, "yellow",
                14, "green",
                15, "magenta"
        );

        for (var entry : colors.entrySet()) {
            Objective objective = mock(Objective.class);
            when(objective.getId()).thenReturn(entry.getKey());

            ProcessedObjective res = AnalizeObjective.analizeObjective(objective);

            assertEquals(ObjectiveType.ARMY_COLOR, res.getType());
            assertEquals(entry.getValue(), res.getObjectiveColor());
        }
    }

    @Test
    void testAnalyzeObjective_IdUnknown() {
        Objective objective = mock(Objective.class);
        when(objective.getId()).thenReturn(99);

        ProcessedObjective result = AnalizeObjective.analizeObjective(objective);

        assertEquals(ObjectiveType.UNKNOWN, result.getType());
        assertEquals(List.of(), result.getTotalContinents());
        assertEquals(Map.of(), result.getCountriesPerContinent());
        assertEquals(List.of(), result.getSingleCountries());
    }
}