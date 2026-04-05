package ar.edu.utn.frc.tup.piii.model.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Representa un mensaje enviado por un jugador durante una partida.
 * Puede ser parte del sistema de chat o comunicación entre jugadores.
 *
 * @author GabrielaCamacho
 * @version 1.0
 *
 * @see Game
 * @see BasePlayer
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private int id;

    @ManyToOne
    @JoinColumn(name = "message_game_id", nullable = false)
    private Game game;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private BasePlayer sender;

    @Column(nullable = false, length = 200)
    private String content;

    @Column(name = "active_state", nullable = false)
    private boolean activeState;

    @Column(name = "modified", nullable = false)
    private boolean modified;

    @Column(name = "date_time_message", nullable = false)
    private LocalDateTime datetime;
}
