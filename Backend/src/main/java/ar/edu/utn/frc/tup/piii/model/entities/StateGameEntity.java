package ar.edu.utn.frc.tup.piii.model.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa el estado actual de una partida del juego. Los estados posibles pueden ser:
 * - preparacion (id: 1)
 * - en curso (id: 2)
 * - hostilidades (id: 3)
 * - finalizada (id: 4)
 * - pausada (id: 5)
 * - cancelada (id: 6)
 * Los valores sé pre-cargan desde la base de datos.
 *
 * @author GabrielaCamacho
 * @version 1.0
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "States_game")
public class StateGameEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "state_id")
    private int id;
    private String description;
}