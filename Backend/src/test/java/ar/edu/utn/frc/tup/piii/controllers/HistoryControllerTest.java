package ar.edu.utn.frc.tup.piii.controllers;

import ar.edu.utn.frc.tup.piii.model.entities.HistoryEvent;
import ar.edu.utn.frc.tup.piii.model.repository.GameRepository;
import ar.edu.utn.frc.tup.piii.services.interfaces.HistoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.*;

@WebMvcTest(HistoryController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class HistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HistoryService historyService;

    @MockitoBean
    private GameRepository gameRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testGetHistoryPerGame() throws Exception {
        int gameId = 1;
        LocalDateTime date = LocalDateTime.of(2025, 6, 30, 12, 0);

        HistoryEvent event = new HistoryEvent();
        event.setDescription("Jugador A atacó a Jugador B");
        event.setDateTime(date);

        Mockito.when(historyService.findAllByGameIdOrderByDateTimeAsc(gameId))
                .thenReturn(List.of(event));

        mockMvc.perform(get("/api/v1/history/{gameId}", gameId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].description", is("Jugador A atacó a Jugador B")))
                .andExpect(jsonPath("$[0].dateTime", containsString("2025-06-30")));
    }
}
