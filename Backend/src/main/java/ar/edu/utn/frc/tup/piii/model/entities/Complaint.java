package ar.edu.utn.frc.tup.piii.model.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 *  Representa una denuncia realizada por un jugador hacia otro durante una partida,
 *  en base a la configuración de reglas de comunicación.
 *
 * @author GabrielaCamacho
 * @version 1.0
 *
 * @see BasePlayer
 * @see Game
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Complaints")
public class Complaint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "complaint_id")
    private int id;

    @ManyToOne
    @JoinColumn(name = "game_complaint_id")
    private Game game;

    @ManyToOne
    @JoinColumn(name = "accuser_id")
    private BasePlayer accuser;

    @ManyToOne
    @JoinColumn(name = "accused_id")
    private BasePlayer accused;

    private String reason;

    @Column(name = "date_time_complaint")
    private LocalDateTime dateTime;
}
