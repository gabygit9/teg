package ar.edu.utn.frc.tup.piii.util;

/**
 * Clase que representa el estado serializado de una partida,
 * se utiliza para encapsular el estado del juego en formato JSON.
 */

public class GameState {
    private final String serializedState;

    public GameState(String serializedState) {
        this.serializedState = serializedState;
    }

    public String getSerializedState() {
        return serializedState;
    }
}

