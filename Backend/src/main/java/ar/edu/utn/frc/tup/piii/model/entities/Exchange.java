package ar.edu.utn.frc.tup.piii.model.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Representa un canje de tarjetas por ejércitos realizado por un jugador
 * durante una partida. Cada canje está vinculado a un jugador (JugadorPartida), está
 * compuesto por varias tarjetas y registra la fecha y la cantidad de ejércitos obtenidos.
 *
 * @author GabrielaCamacho
 * @version 1.0
 *
 * @see PlayerGame
 * @see CardsExchange
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Exchanges")
public class Exchange {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exchange_id")
    private int id;

    @ManyToOne
    @JoinColumn(name = "player_card_exchange_id")
    private PlayerGame playerGame;

    @Column(name = "date_time_exchange")
    private LocalDateTime dateTime;

    @Column(name = "army_amount")
    private int armyAmount;

    @OneToMany(mappedBy = "exchange", cascade = CascadeType.ALL)
    private List<CardsExchange> cardsExchange;
}
