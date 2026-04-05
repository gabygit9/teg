package ar.edu.utn.frc.tup.piii.model.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

/**
 * Esta clase representa la participación de un jugador (humano o bot) en una partida específica.
 * Contiene información contextual como el color asignado, el objetivo secreto,
 * su estado dentro de la partida, el orden en el que juega y si le corresponde el turno actual.
 *
 * Se vincula con los países que controla y las tarjetas que posee durante dicha partida.

 * {@code @author:} Ismael Ceballos
 * @see BasePlayer
 * @see Color
 * @see Objective
 * @see Game
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Players_game")
@ToString(onlyExplicitlyIncluded = true)
public class PlayerGame {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "player_id")
    @ToString.Include
    private int id;

    @ManyToOne
    @JoinColumn(name = "game_id")
    private Game game;

    // originalmente tenia este decorador pero lo cambie por el otro por problemas con Memento, de haber problemas,
    // vuelvan al original
    //@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "base_player_id")
    private BasePlayer player;

    @ManyToOne
    @JoinColumn(name = "color_id")
    private Color color;

    @ManyToOne
    @JoinColumn(name = "objective_id")
    private Objective secretObjective;

    @Column(name = "objective_achieved")
    private boolean objectiveAchieved;

    @Column(name = "order_turn")
    private int orderTurn;

    @Column(name = "is_turn")
    private boolean isTurn;

    @OneToMany(mappedBy = "playerGame")
    @ToString.Exclude
    private List<CountryGame> countries;

    @OneToMany(mappedBy = "playerGame")
    private List<CardPlayer> cards;

    private boolean active = true;

}
