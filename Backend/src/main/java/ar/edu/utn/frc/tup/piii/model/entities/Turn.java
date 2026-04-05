package ar.edu.utn.frc.tup.piii.model.entities;

import ar.edu.utn.frc.tup.piii.model.enums.TurnPhase;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 *  Representa un turno dentro de una partida.
 *  Cada turno pertenece a un jugador en una partida específica e indica la fase actual.
 *
 * @author GabrielaCamacho
 * @version 1.0
 *
 * @see PlayerGame
 * @see Game
 * @see TurnPhase
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Turns")
public class Turn {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "turn_id")
    private int id;

    @ManyToOne
    @JoinColumn(name = "turn_player_game_id", nullable = false)
    private PlayerGame playerGame;

    @ManyToOne
    @JoinColumn(name = "turn_game_id", nullable = false)
    private Game game;


    @Enumerated(EnumType.STRING)
    @Column(name = "current_phase", nullable = false)
    private TurnPhase currentPhase; /////VER

    @Column(name = "initial_start_date", nullable = false)
    private LocalDateTime initialStartDate;

    @Column(name = "max_duration", nullable = false)
    private int maxDuration; // en segundos

    @Column(name = "available_armies", nullable = false)
    private int availableArmies;

    private Boolean finished;
}
