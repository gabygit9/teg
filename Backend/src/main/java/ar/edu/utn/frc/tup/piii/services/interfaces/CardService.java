package ar.edu.utn.frc.tup.piii.services.interfaces;

import ar.edu.utn.frc.tup.piii.model.entities.CardPlayer;
import ar.edu.utn.frc.tup.piii.model.entities.CardCountry;
import jakarta.transaction.Transactional;

import java.util.List;

/**
 * Interfaz que define las operaciones relacionadas con las tarjetas de país en el juego.
 * Las tarjetas permiten obtener refuerzos mediante combinaciones válidas y son asignadas
 * como recompensa por conquistas.
 *
 * @author Ismael Ceballos
 */
public interface CardService {

    boolean updateCountry(CardCountry card);

    CardCountry findByIdCountry(int id);

    List<CardCountry> findAllCountry();

    boolean savePlayer(CardPlayer card);

    boolean updatePlayer(CardPlayer card);

    CardPlayer findByIdPlayer(int id);

    List<CardPlayer> findAllPlayer();

    /**
     * Devuelve una tarjeta de país aleatoria que no haya sido asignada.
     *
     * @return TarjetaPais disponible o null si no hay.
     */
    CardCountry getAvailableCard();

    /**
     * Asigna una tarjeta de país a un jugador en la partida.
     *
     * @param cardId        Tarjeta de país.
     * @param playerGameId Jugador que la recibe.
     * @return
     */
    CardCountry assignCardToPlayer(int cardId, int playerGameId);

    /**
     * Marca como usada una tarjeta de jugador (luego de un canje).
     *
     * @param playerCardId Tarjeta a marcar como usada.
     */
    void markCardAsUsed(int playerCardId);

    /**
     * Obtiene todas las tarjetas que posee un jugador en una partida.
     *
     * @param playerGameId Jugador a consultar.
     * @return Lista de tarjetas en su poder.
     */
    List<CardPlayer> getPlayerCards(int playerGameId);


    /**
     * Verifica si un jugador tiene una combinación válida de tarjetas para realizar un canje.
     *
     * @param cardsIds Lista de tarjetas seleccionadas.
     * @param playerGameId Jugador que realiza el intento de canje.
     * @return true si es una combinación válida, false si no lo es.
     */
    boolean isValidExchange(List<Integer> cardsIds, int playerGameId);

    void askCard(int playerId);
    void doExchange(int playerId, int gameId);


    void saveAll(List<CardPlayer> cards);

    void giveArmiesWithCards(int gameId, int playerGameId, int cardId);

    List<CardPlayer> getCardsByIds(List<Integer> ids);

    @Transactional
    void deletePlayerCards(List<CardPlayer> cards);

    void markConquer(int id);
}
