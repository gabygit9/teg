package ar.edu.utn.frc.tup.piii.model.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 *  Representa una tarjeta de país que ha sido entregada a un jugador
 *  en una partida determinada. Estas tarjetas pueden ser canjeadas
 *  por ejércitos si cumplen las reglas de combinación.
 *
 * @author GabrielaCamacho
 * @version 1.0
 *
 * @see CardCountry
 * @see PlayerGame
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Cards_player")
public class CardPlayer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "card_player_id")
    private int id;

    @ManyToOne
    @JoinColumn(name = "card_country_id")
    @ToString.Exclude
    private CardCountry cardCountry;

    @ManyToOne
    @JoinColumn(name = "player_game_id")
    @ToString.Exclude
    private PlayerGame playerGame;

    private boolean used;
}
