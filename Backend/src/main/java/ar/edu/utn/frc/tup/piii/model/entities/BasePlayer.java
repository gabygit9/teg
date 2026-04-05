package ar.edu.utn.frc.tup.piii.model.entities;

import jakarta.persistence.*;
import lombok.*;

/**
 * Clase abstracta que representa la estructura base de cualquier jugador (humano o bot).
 * Esta clase será extendida por implementaciones concretas como JugadorHumano o JugadorBot.
 * {@code @author:} Ismael Ceballos
 */

@Entity
@Table(name = "Base_players")
@Getter
@Setter
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class BasePlayer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "base_player_id")
    private int id;

    @Column(name = "name_player")
    private String name;

    @Column(name = "available_armies")
    private int availableArmies;

}