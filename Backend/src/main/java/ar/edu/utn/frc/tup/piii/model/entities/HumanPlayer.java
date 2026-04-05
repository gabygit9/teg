package ar.edu.utn.frc.tup.piii.model.entities;

import jakarta.persistence.*;
import lombok.*;

/**
 * Representa a un jugador humano dentro del juego.
 * No tiene lógica de IA, las decisiones son tomadas por el usuario.
 *
 * @author GabrielaCamacho
 * @version 1.0
 *
 * @see BasePlayer
 */

@Entity
@Table(name = "Human_players")
@PrimaryKeyJoinColumn(name = "human_player_id", referencedColumnName = "base_player_id")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HumanPlayer extends BasePlayer {
    @OneToOne(optional = false)
    @JoinColumn(name = "user_player_id", referencedColumnName = "user_id")
    private User user;
}
