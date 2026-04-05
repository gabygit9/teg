package ar.edu.utn.frc.tup.piii.model.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Representa una sesión del juego TEG.
 * Esta clase almacena información general sobre una partida, incluyendo su fecha de
 * inicio, el estado actual, la modalidad de comunicación y el objetivo común.
 *
 * @author GabrielaCamacho
 * @version 1.0
 *
 * @see StateGameEntity
 * @see CommunicationType
 * @see Objective
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Games")
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "game_id")
    private int id;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "start_date_game")
    private LocalDateTime startDate;

    @ManyToOne
    @JoinColumn(name = "state_id")
    private StateGameEntity states;

    @ManyToOne
    @JoinColumn(name = "communication_type_id")
    private CommunicationType communicationType;

    @ManyToOne
    @JoinColumn(name = "objective_common_id")
    private Objective objectiveCommon;
}
