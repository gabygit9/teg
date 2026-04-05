package ar.edu.utn.frc.tup.piii.model.entities;

import jakarta.persistence.*;
import lombok.*;

/**
 *  Clase abstracta que representa a un jugador controlado por la IA (bot).
 *  Debe ser extendida por tipos concretos de bots con diferentes estrategias.
 *
 * @author GabrielaCamacho
 * @version 1.0
 *
 * @see LevelBot
 * @see BasePlayer
 */
@Table(name = "Players_bot")
@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class BotPlayer extends BasePlayer {

    @ManyToOne
    @JoinColumn(name = "level_bot_id")
    private LevelBot levelBot;

}
