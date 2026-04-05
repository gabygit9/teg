package ar.edu.utn.frc.tup.piii.model.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa un símbolo asociado a una tarjeta de país.
 *  Ejemplos de símbolos:
 *  - Cañón
 *  - Globo
 *  - Galeón
 *
 * @author GabrielaCamacho
 * @version 1.0
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Symbols")
public class Symbol {
    @Id
    @Column(name = "symbol_id")
    private int id;
    @Column(name = "type_symbol")
    private String type;
}
