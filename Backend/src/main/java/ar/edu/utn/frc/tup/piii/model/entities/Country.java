package ar.edu.utn.frc.tup.piii.model.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *  Representa un país del mapa en el juego TEG.
 *  Cada país pertenece a un continente.
 *
 * @author GabrielaCamacho
 * @version 1.0
 *
 * @see Continent
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Countries")
public class Country {
    @Id
    @Column(name = "country_id")
    private int id;

    @Column(name = "name_country")
    private String name;

    @ManyToOne
    @JoinColumn(name = "continent_country_id", nullable = false)
    private Continent continent;
}
