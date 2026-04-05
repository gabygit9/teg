package ar.edu.utn.frc.tup.piii.services.interfaces;

import ar.edu.utn.frc.tup.piii.dto.PactDTO;
import ar.edu.utn.frc.tup.piii.model.entities.Pact;

import java.util.List;

/**
 * Interfaz que define las operaciones para la gestión de pactos durante la partida.
 *
 * Permite crear, romper, validar pactos y consultar pactos activos.
 *
 * @author Ismael Ceballos
 */
public interface PactService {

    boolean save(Pact pact);

    boolean update(Pact pact);

    Pact findById(int id);

    List<Pact> findAll();

    /**
     * Crea un nuevo pacto entre jugadores.
     *
     * @param pactDTO Objeto que contiene los datos del pacto.
     * @return El pacto creado.
     */
    Pact createPact(PactDTO pactDTO);

    /**
     * Rompe (inactiva) un pacto existente por su identificador.
     *
     * @param pactId Identificador del pacto a romper.
     * @return true si se rompió correctamente, false en caso contrario.
     */
    boolean brokePact(int pactId);

    /**
     * Devuelve todos los pactos activos de una partida.
     *
     * @param gameId ID de la partida.
     * @return Lista de pactos activos relacionados a la partida.
     */
    List<Pact> getPactsByGame(int gameId);

}
