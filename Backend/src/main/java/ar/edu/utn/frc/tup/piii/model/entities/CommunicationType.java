package ar.edu.utn.frc.tup.piii.model.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa el tipo de comunicación permitida entre los jugadores
 * durante una partida del juego.
 *
 * Ejemplos de tipos de comunicación:
 * - Fair_Play
 * - Vale_Todo
 *
 * Los valores suelen estar precargados en la base de datos.
 *
 * @author GabrielaCamacho
 * @version 1.0
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Communication_types")
public class CommunicationType {

    @Id
    @Column(name = "communication_id")
    private int id;
    private String description;
}
