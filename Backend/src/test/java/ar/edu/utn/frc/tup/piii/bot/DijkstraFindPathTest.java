package ar.edu.utn.frc.tup.piii.bot;

import ar.edu.utn.frc.tup.piii.model.entities.CountryGame;
import ar.edu.utn.frc.tup.piii.model.entities.CountryGameId;
import ar.edu.utn.frc.tup.piii.services.interfaces.CountryGameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class DijkstraFindPathTest {

    @Mock
    private CountryGameService countryGameService;

    private CountryGame p1, p2, p3, p4;

    @BeforeEach
    void setUp() {
        p1 = new CountryGame(); p1.setId(new CountryGameId(1, 1));
        p2 = new CountryGame(); p2.setId(new CountryGameId(2, 1));
        p3 = new CountryGame(); p3.setId(new CountryGameId(3, 1));
        p4 = new CountryGame(); p4.setId(new CountryGameId(4, 1));
    }

    @Test
    void testOriginNull_returnEmpty() {
        List<CountryGame> res = DijkstraFindPath.findShorterPath(null, p2, List.of(p1, p2), countryGameService);
        assertTrue(res.isEmpty());
    }

    @Test
    void testOriginEqualDestination() {
        List<CountryGame> res = DijkstraFindPath.findShorterPath(p1, p1, List.of(p1, p2), countryGameService);
        assertEquals(List.of(p1), res);
    }

    @Test
    void testCaminoSimple() {
        List<CountryGame> all = Arrays.asList(p1, p2, p3);

        when(countryGameService.getBorder(eq(p1), anyList())).thenReturn(new CountryGame[]{p2});
        when(countryGameService.getBorder(eq(p2), anyList())).thenReturn(new CountryGame[]{p1, p3});
        when(countryGameService.getBorder(eq(p3), anyList())).thenReturn(new CountryGame[]{p2});

        List<CountryGame> res = DijkstraFindPath.findShorterPath(p1, p3, all, countryGameService);

        assertEquals(List.of(p1, p2, p3), res);
    }

    @Test
    void testWithouPath() {
        List<CountryGame> all = List.of(p1, p2, p3);

        when(countryGameService.getBorder(eq(p1), anyList())).thenReturn(new CountryGame[]{p2});
        when(countryGameService.getBorder(eq(p2), anyList())).thenReturn(new CountryGame[]{p1});
        when(countryGameService.getBorder(eq(p3), anyList())).thenReturn(new CountryGame[]{});

        List<CountryGame> res = DijkstraFindPath.findShorterPath(p1, p3, all, countryGameService);

        assertTrue(res.isEmpty());
    }

    @Test
    void testSelectedCycleShorterPath() {
        List<CountryGame> all = List.of(p1, p2, p3, p4);

        when(countryGameService.getBorder(eq(p1), anyList())).thenReturn(new CountryGame[]{p2, p3});
        when(countryGameService.getBorder(eq(p2), anyList())).thenReturn(new CountryGame[]{p1, p4});
        when(countryGameService.getBorder(eq(p3), anyList())).thenReturn(new CountryGame[]{p1, p4});
        when(countryGameService.getBorder(eq(p4), anyList())).thenReturn(new CountryGame[]{p2, p3});

        List<CountryGame> res = DijkstraFindPath.findShorterPath(p1, p4, all, countryGameService);

        // Camino válido puede ser p1→p2→p4 o p1→p3→p4
        assertEquals(3, res.size());
        assertEquals(p1, res.get(0));
        assertEquals(p4, res.get(2));
    }
}