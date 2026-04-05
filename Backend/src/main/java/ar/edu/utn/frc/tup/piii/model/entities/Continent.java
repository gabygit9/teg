package ar.edu.utn.frc.tup.piii.model.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa un continente en el tablero del juego
 *
 * @author GabrielaCamacho
 * @version 1.0
 *
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Continents")
public class Continent {
    @Id
    @Column(name = "continent_id")
    private int id;

    @Column(name = "name_continent")
    private String name;

    @Column(name = "army_bonus")
    private int armyBonus;
}
