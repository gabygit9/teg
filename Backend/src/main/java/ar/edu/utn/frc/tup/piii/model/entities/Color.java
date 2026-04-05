package ar.edu.utn.frc.tup.piii.model.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

/**
 * Representa un color disponible en el juego.
 * Este color se asigna a un jugador dentro de una partida específica.
 * {@code @author:} Ismael Ceballos
 *
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Colors")
public class Color {
    @Id
    @Column(name = "color_id")
    private int id;

    @Column(name = "name_color")
    private String name;
}
