package ar.edu.utn.frc.tup.piii.services.interfaces;

import ar.edu.utn.frc.tup.piii.model.entities.Complaint;
import ar.edu.utn.frc.tup.piii.model.entities.Game;
import ar.edu.utn.frc.tup.piii.model.entities.BasePlayer;

import java.util.List;

/**
 * Interfaz que define los métodos para gestionar denuncias dentro de una partida.
 *
 * Este servicio permite registrar y consultar denuncias realizadas por los jugadores,
 * en el contexto del sistema de comunicación (Fair Play / Vale Todo).
 *
 * @author Ismael Ceballos
 * @version 1.0
 */
public interface ComplaintService {

    boolean save(Complaint complaint);

    boolean update(Complaint complaint);

    Complaint findById(int id);

    List<Complaint> findAll();
    /**
     * Registra una nueva denuncia entre jugadores.
     *
     * @param game Partida en la que ocurrió la infracción.
     * @param accuser Jugador que realiza la denuncia.
     * @param accused Jugador denunciado.
     * @param reason Descripción del reason de la denuncia.
     * @return La denuncia registrada.
     */
    Complaint registerComplaint(Game game, BasePlayer accuser, BasePlayer accused, String reason);

    /**
     * Devuelve todas las denuncias realizadas en una partida específica.
     *
     * @param game Partida consultada.
     * @return Lista de denuncias.
     */
    List<Complaint> getComplaintsByGame(Game game);
}
