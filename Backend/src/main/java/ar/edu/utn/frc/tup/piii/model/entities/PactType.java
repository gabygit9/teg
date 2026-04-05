package ar.edu.utn.frc.tup.piii.model.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa el tipo de pacto que pueden hacer los jugadores durante una partida.
 *  *
 * Ejemplos:
 * - No agresión
 * - Pacto entre países
 * - Zona internacional
 *
 * @author GabrielaCamacho
 * @version 1.0
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Pact_types")
public class PactType {
    @Id
    @Column(name = "pact_type_id")
    private int id;
    private String description;
}
