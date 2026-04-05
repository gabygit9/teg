package ar.edu.utn.frc.tup.piii.model.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardsExchangeId implements Serializable {

    @Column(name = "exchange_d")
    private int exchangeId;

    @Column(name = "player_card_id")
    private int playerCardId;
}
