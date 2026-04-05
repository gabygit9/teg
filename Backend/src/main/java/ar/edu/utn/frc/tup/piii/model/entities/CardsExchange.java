package ar.edu.utn.frc.tup.piii.model.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *  Representa una tarjeta específica utilizada en un canje.
 *  Cada canje tiene 3 tarjetas asociadas mediante esta clase.
 *
 * @author GabrielaCamacho
 * @version 1.0
 *
 * @see Exchange
 * @see CardPlayer
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Exchange_cards")
public class CardsExchange {

    @EmbeddedId
    private CardsExchangeId id;

    @ManyToOne
    @MapsId("exchangeId") // vincula con la clave embebida
    @JoinColumn(name = "exchange_id")
    private Exchange exchange;

    @ManyToOne
    @MapsId("playerCardId")
    @JoinColumn(name = "player_card_id")
    private CardPlayer cardPlayer;
}

