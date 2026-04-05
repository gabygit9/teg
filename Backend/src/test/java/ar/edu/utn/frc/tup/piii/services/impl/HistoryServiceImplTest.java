package ar.edu.utn.frc.tup.piii.services.impl;

import ar.edu.utn.frc.tup.piii.model.entities.Game;
import ar.edu.utn.frc.tup.piii.model.entities.HistoryEvent;
import ar.edu.utn.frc.tup.piii.model.repository.HistoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
class HistoryServiceImplTest {

    @Mock
    private HistoryRepository historyRepository;

    @InjectMocks
    HistoryServiceImpl historyService;

    @Test
    void historyFindByIdTest() {
        HistoryEvent event = new HistoryEvent();
        event.setId(1);

        when(historyRepository.findById(1)).thenReturn(Optional.of(event));

        HistoryEvent result = historyService.findById(1);

        assertNotNull(result);
        assertEquals(event, result);

    }

    @Test
    void historyFindAllByGameTest() {
        Game game = new Game();
        game.setId(1);
        HistoryEvent event1 = new HistoryEvent();
        event1.setId(1);
        event1.setGame(game);
        HistoryEvent event2 = new HistoryEvent();
        event2.setId(2);
        event2.setGame(game);

        List<HistoryEvent> listEvents = List.of(event1, event2);

        when(historyRepository.findAllByGameId(1)).thenReturn(listEvents);

        List<HistoryEvent> result = historyRepository.findAllByGameId(1);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(event1));
        assertTrue(result.contains(event2));
    }

    @Test
    void historyRegisterTest() {
        Game game = new Game();
        game.setId(1);
        String description = "Evento de prueba";

        HistoryEvent[] eventSaved = new HistoryEvent[1];


        when(historyRepository.save(ArgumentMatchers.<HistoryEvent>any()))
                .thenAnswer(invoc -> {
                    eventSaved[0] = invoc.getArgument(0);
                    return eventSaved[0];
                });

        historyService.registerEvent(game, description);

        assertNotNull(eventSaved[0]);
        assertEquals(game, eventSaved[0].getGame());
        assertEquals(description, eventSaved[0].getDescription());
        assertNotNull(eventSaved[0].getDateTime());
    }

    @Test
    void historySaveTest() {
        Game game = new Game();
        game.setId(1);

        HistoryEvent event = new HistoryEvent();
        event.setGame(game);
        event.setDescription("Evento de prueba");
        event.setDateTime(LocalDateTime.now());
        event.setId(1);

        when(historyRepository.save(ArgumentMatchers.<HistoryEvent>any()))
                .thenAnswer(invoc -> invoc.getArgument(0));

        boolean result = historyService.save(event);

        assertTrue(result);
        verify(historyRepository, times(1)).save(event);
    }

    @Test
    void historyDoesntExistsUpdateTest() {
        Game game = new Game();
        game.setId(1);

        HistoryEvent event = new HistoryEvent();
        event.setGame(game);
        event.setDescription("Evento de prueba");
        event.setDateTime(LocalDateTime.now());
        event.setId(1);

        when(historyRepository.existsById(1)).thenReturn(false);

        boolean result = historyService.update(event);

        assertFalse(result);
        verify(historyRepository).existsById(1);
    }

    @Test
    void historyExistsUpdateTest() {
        Game game = new Game();
        game.setId(1);

        HistoryEvent event = new HistoryEvent();
        event.setGame(game);
        event.setDescription("Evento actualizado");
        event.setDateTime(LocalDateTime.now());
        event.setId(1);

        when(historyRepository.existsById(1)).thenReturn(true);

        when(historyRepository.save(ArgumentMatchers.<HistoryEvent>any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        boolean result = historyService.update(event);

        assertTrue(result);
        verify(historyRepository).existsById(1);
        verify(historyRepository).save(event);
    }

}