package ar.edu.utn.frc.tup.piii.services.impl;

import ar.edu.utn.frc.tup.piii.model.entities.StateGameEntity;
import ar.edu.utn.frc.tup.piii.model.repository.GameStateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StateGameServiceImplTest {

    @Mock
    private GameStateRepository gameStateRepository;

    @InjectMocks
    private GameStateServiceImpl gameStateService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findByIdTest() {
        StateGameEntity state = new StateGameEntity(1, "IN_COURSE");

        when(gameStateRepository.findById(1)).thenReturn(Optional.of(state));

        StateGameEntity result = gameStateService.findById(1);

        assertNotNull(result);
        assertEquals("IN_COURSE", result.getDescription());
    }

    @Test
    void findByIdDoesntExistsTest() {
        when(gameStateRepository.findById(99)).thenReturn(Optional.empty());

        StateGameEntity result = gameStateService.findById(99);

        assertNull(result);
    }

    @Test
    void findAllTest() {
        StateGameEntity e1 = new StateGameEntity(1, "IN_COURSE");
        StateGameEntity e2 = new StateGameEntity(2, "FINISHED");

        when(gameStateRepository.findAll()).thenReturn(Arrays.asList(e1, e2));

        List<StateGameEntity> list = gameStateService.findAll();

        assertEquals(2, list.size());
        assertEquals("IN_COURSE", list.get(0).getDescription());
    }

    @Test
    void findByDescriptionTest() {
        StateGameEntity state = new StateGameEntity(1, "FINISHED");

        when(gameStateRepository.findByDescription("FINISHED")).thenReturn(state);

        StateGameEntity result = gameStateService.findByDescription("FINISHED");

        assertNotNull(result);
        assertEquals("FINISHED", result.getDescription());
    }

    @Test
    void findByDescriptionIgnoreCaseTest() {
        StateGameEntity state = new StateGameEntity(2, "IN_COURSE");

        when(gameStateRepository.findByDescriptionIgnoreCase("IN_COURSE")).thenReturn(Optional.of(state));

        Optional<StateGameEntity> result = gameStateService.findByDescriptionIgnoreCase("IN_COURSE");

        assertTrue(result.isPresent());
        assertEquals("IN_COURSE", result.get().getDescription());
    }

    @Test
    void findByDescriptionIgnoreCaseDoesntExistsTest() {
        when(gameStateRepository.findByDescriptionIgnoreCase("inexistente")).thenReturn(Optional.empty());

        Optional<StateGameEntity> result = gameStateService.findByDescriptionIgnoreCase("inexistente");

        assertTrue(result.isEmpty());
    }
}
