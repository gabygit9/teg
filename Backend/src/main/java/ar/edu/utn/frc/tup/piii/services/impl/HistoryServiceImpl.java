package ar.edu.utn.frc.tup.piii.services.impl;

import ar.edu.utn.frc.tup.piii.model.entities.Game;
import ar.edu.utn.frc.tup.piii.model.entities.HistoryEvent;
import ar.edu.utn.frc.tup.piii.model.repository.HistoryRepository;
import ar.edu.utn.frc.tup.piii.services.interfaces.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementación del servicio que gestiona el historial de eventos del juego.
 *
 * Este servicio registra cada acción relevante que ocurre durante una partida
 * (ataques, conquistas, canjes, cumplimiento de objetivos, etc.),
 * permitiendo luego su consulta para revisión o trazabilidad.
 *
 * @see HistoryEvent
 * @see Game
 * @author Grupo 7
 */
@Service
@RequiredArgsConstructor
public class HistoryServiceImpl implements HistoryService {

    private final HistoryRepository historyRepository;

    @Override
    public boolean save(HistoryEvent history){
        HistoryEvent historySave = historyRepository.save(history);
        return historySave.getId() > 0;
    }

    @Override
    public boolean update(HistoryEvent history) {
        if (historyRepository.existsById(history.getId())) {
            historyRepository.save(history);
            return true;
        }
        return false;
    }


    @Override
    public HistoryEvent findById(int id) {
        return historyRepository.findById(id).orElse(null);
    }

    @Override
    public void registerEvent(Game game, String description) {
        HistoryEvent event = new HistoryEvent();
        event.setGame(game);
        event.setDescription(description);
        event.setDateTime(LocalDateTime.now());
        historyRepository.save(event);
    }

    @Override
    public List<HistoryEvent> findAllByGameIdOrderByDateTimeAsc(int gameId) {
        return historyRepository.findAllByGameId(gameId);
    }

}