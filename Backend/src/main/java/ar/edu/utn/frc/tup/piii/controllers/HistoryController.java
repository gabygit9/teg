package ar.edu.utn.frc.tup.piii.controllers;

import ar.edu.utn.frc.tup.piii.dto.EventRequestDto;
import ar.edu.utn.frc.tup.piii.model.entities.HistoryEvent;
import ar.edu.utn.frc.tup.piii.services.interfaces.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador para el historial de eventos de una partida
 */

@RestController
@RequestMapping("/api/v1/history")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;

    @GetMapping("/{gameId}")
    public ResponseEntity<List<EventRequestDto>> getHistoryPerGame(@PathVariable int gameId) {
        List<HistoryEvent> history = historyService.findAllByGameIdOrderByDateTimeAsc(gameId);

        List<EventRequestDto> summary = history.stream()
                .map(e -> new EventRequestDto(e.getDescription(), e.getDateTime()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(summary);
    }

}

