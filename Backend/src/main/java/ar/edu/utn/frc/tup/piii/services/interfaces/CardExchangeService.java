package ar.edu.utn.frc.tup.piii.services.interfaces;

import ar.edu.utn.frc.tup.piii.model.entities.CardsExchange;
import jakarta.transaction.Transactional;

import java.util.List;

/**
 * Servicio que gestiona la lógica de canje de tarjetas por ejércitos.
 *
 *  @author Ismael Ceballos
 *  @version 1.0
 */
public interface CardExchangeService {

    boolean save(CardsExchange user);

    CardsExchange findById(int id);

    List<CardsExchange> findAll();

    /**
     * Realiza un canje de tarjetas por ejércitos para el jugador indicado.
     *
     * @param playerGameId ID del jugador en la partida.
     */
    boolean doExchange(int playerGameId, int gameId);

    /**
     * Verifica si el jugador puede realizar un canje en su turno actual.
     *
     * @param playerGameId ID del jugador en la partida.
     * @return true si puede canjear, false en caso contrario.
     */
    boolean canExchange(int playerGameId);

    /**
     * Obtiene la cantidad de ejércitos que recibirá un jugador por su próximo canje.
     *
     * @param playerGameId ID del jugador en la partida.
     * @return Cantidad de ejércitos a otorgar.
     */
    int getArmiesByNextExchange(int playerGameId);


    /**
     * Verifica si un jugador tiene una combinación válida de tarjetas para realizar un canje.
     * Combinaciones:
     *              - Tres tarjetas con iguales símbolos.
     *              - Tres tarjetas  con distintos símbolos.
     *
     * @param cardsIds Lista de tarjetas seleccionadas.
     * @param playerGameId Jugador que realiza el intento de canje.
     * @return true si es una combinación válida, false si no lo es.
     */
    boolean isValidExchange(List<Integer> cardsIds, int playerGameId);


    @Transactional
    boolean exchangeCards(List<Integer> cardsIds, int playerGameId, int gameId);
}
