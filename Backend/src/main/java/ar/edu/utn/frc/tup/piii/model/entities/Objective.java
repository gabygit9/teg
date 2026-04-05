package ar.edu.utn.frc.tup.piii.model.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Esta clase representa el objetivo secreto de un jugador dentro de una partida.
 * {@code @author:} Ismael Ceballos
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Secrets_objectives")
public class Objective {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "objective_id")
    private int id;
    private String description;
}
