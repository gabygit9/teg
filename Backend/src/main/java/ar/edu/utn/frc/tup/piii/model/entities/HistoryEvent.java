package ar.edu.utn.frc.tup.piii.model.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Representa un evento registrado durante una partida (como ataques, canjes, cumplimiento de objetivo, etc.).
 *
 * @author GabrielaCamacho
 * @version 1.0
 *
 * @see Game
 */
@Entity
@Table(name = "History_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoryEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private int id;

    @ManyToOne
    @JoinColumn(name = "history_game_id")
    private Game game;

    private String description;

    @Column(name = "date_time_event")
    private LocalDateTime dateTime;
}
