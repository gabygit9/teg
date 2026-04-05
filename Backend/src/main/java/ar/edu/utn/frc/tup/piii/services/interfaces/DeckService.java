package ar.edu.utn.frc.tup.piii.services.interfaces;

import ar.edu.utn.frc.tup.piii.model.entities.CardCountry;

import java.util.List;

public interface DeckService {

    /**
     * Inicializa el mazo con todas las tarjetas de países disponibles.
     * Este método se debe llamar al inicio de cada partida para preparar
     * el mazo con todas las cartas barajadas.
     */
    void initDeck();

    /**
     * Inicializa el mazo para una partida específica.
     * Permite tener mazos independientes por partida.
     *
     * @param gameId ID de la partida para la cual inicializar el mazo
     */
    void initDeckToGame(int gameId);

    /**
     * Obtiene la próxima tarjeta disponible del mazo.
     * Se entrega cuando un jugador conquista un país durante su turno.
     *
     * @return La próxima tarjeta disponible, o null si el mazo está vacío
     */
    CardCountry getAvailableCards();

    /**
     * Obtiene la próxima tarjeta disponible del mazo para una partida específica.
     *
     * @param gameId ID de la partida
     * @return La próxima tarjeta disponible para esa partida, o null si el mazo está vacío
     */
    CardCountry getAvailableCardsToGame(int gameId);

    /**
     * Mezcla las cartas del mazo de forma aleatoria.
     * Útil al inicio de la partida o cuando se reinicia el mazo.
     */
    void shuttleDeck();


    /**
     * Cuenta la cantidad de tarjetas que quedan disponibles en el mazo.
     *
     * @return Número de tarjetas disponibles (no asignadas a jugadores)
     */
    int countAvailableCards();

    /**
     * Cuenta la cantidad de tarjetas disponibles para una partida específica.
     *
     * @param gameId ID de la partida
     * @return Número de tarjetas disponibles en esa partida
     */
    int countAvailableCardsToGame(int gameId);

    /**
     * Reinicializa el mazo recuperando todas las tarjetas usadas.
     * Se ejecuta cuando el mazo se queda sin cartas pero aún hay jugadores
     * que necesitan recibir tarjetas por conquistas.
     */
    void restartDeck();

    /**
     * Reinicializa el mazo para una partida específica.
     *
     * @param gameId ID de la partida
     */
    void restartDeckToGame(int gameId);

    /**
     * Verifica si el mazo está vacío (sin tarjetas disponibles).
     *
     * @return true si no quedan tarjetas disponibles, false en caso contrario
     */
    boolean isEmptyDeck();

    /**
     * Verifica si el mazo está vacío para una partida específica.
     *
     * @param gameId ID de la partida
     * @return true si no quedan tarjetas disponibles en esa partida, false en caso contrario
     */
    boolean isEmptyDeckToGame(int gameId);

    /**
     * Obtiene todas las tarjetas que actualmente están en el mazo (disponibles).
     * Útil para debugging o para mostrar información del estado del mazo.
     *
     * @return Lista de tarjetas actualmente disponibles en el mazo
     */
    List<CardCountry> getCardsInDeck();

    /**
     * Devuelve una cardCountry al mazo.
     * Se usa cuando un jugador abandona la partida y hay que devolver sus cartas,
     * o en situaciones especiales del juego.
     *
     * @param cardCountry La cardCountry a devolver al mazo
     */
    void returnCardToDeck(CardCountry cardCountry);
}