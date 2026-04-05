package ar.edu.utn.frc.tup.piii.dto.mementosDTOs;
import ar.edu.utn.frc.tup.piii.dto.PlayerGameMementoDTO;
import ar.edu.utn.frc.tup.piii.dto.CountryGameMementoDTO;
import ar.edu.utn.frc.tup.piii.model.entities.*;
import lombok.Data;
import java.util.List;

/**
 * DTO que representa el estado completo del juego en un momento determinado,
 * utilizado para restaurar una partida desde un Memento
 */

@Data
public class GameStateMementoDTO {

    // Cuando el front esté hecho, comprobar que con estas tablas sea suficiente para recuperar la partida sin faltante
    // de datos.
    private Game game;
    private List<PlayerGameMementoDTO> players;
    private List<CountryGameMementoDTO> countries;
    private List<TurnMementoDTO> turns;
    private List<PactMementoDTO> pacts;
    private List<MessageMementoDTO> messages;
    private List<HistoryMementoDTO> history;

    public GameStateMementoDTO() {
    }

    public GameStateMementoDTO(Game game,
                               List<PlayerGameMementoDTO> players,
                               List<CountryGameMementoDTO> countries,
                               List<TurnMementoDTO> turns,
                               List<PactMementoDTO> pacts,
                               List<MessageMementoDTO> messages,
                               List<HistoryMementoDTO> history) {
        this.game = game;
        this.players = players;
        this.countries = countries;
        this.turns = turns;
        this.pacts = pacts;
        this.messages = messages;
        this.history = history;
    }

    }