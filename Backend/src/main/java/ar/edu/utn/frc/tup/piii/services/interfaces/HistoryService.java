package ar.edu.utn.frc.tup.piii.services.interfaces;

import ar.edu.utn.frc.tup.piii.model.entities.Game;
import ar.edu.utn.frc.tup.piii.model.entities.HistoryEvent;

import java.util.List;

/**
 * Interfaz que define las operaciones del historial de eventos de una partida.
 *
 * Permite registrar acciones importantes realizadas durante el desarrollo del juego
 * y consultar la cronología de eventos por partida.
 *
 * @author Ismael Ceballos
 */
public interface HistoryService {

    boolean save(HistoryEvent history);

    boolean update(HistoryEvent history);

    HistoryEvent findById(int id);


    /**
     * Registra un evento en el historial de la partida.
     *
     * @param game Partida donde ocurrió el evento.
     * @param description Descripción del evento (ej: "Jugador X atacó Brasil").
     * @return El evento registrado.
     */
    void registerEvent(Game game, String description);

    /**
     * Obtiene todos los eventos registrados para una partida.
     *
     * @param gameId Partida a consultar.
     * @return Lista de eventos ordenados por fecha.
     */
    List<HistoryEvent> findAllByGameIdOrderByDateTimeAsc(int gameId);


}
