package ar.edu.utn.frc.tup.piii.model.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Representa una tarjeta que está asociada a un país específico y a un símbolo.
 * Estas tarjetas pueden ser entregadas a los jugadores al final de un turno exitoso
 * y son utilizadas para realizar canjes por ejércitos
 *
 * @author GabrielaCamacho
 * @version 1.0
 *
 * @see Country
 * @see Symbol
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Cards_country")
public class CardCountry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "card_country_id")
    private int id;
    @ManyToOne
    @JoinColumn(name = "country_id")
    @ToString.Exclude
    private Country country;
    @ManyToOne
    @JoinColumn(name = "symbol_id")
    @ToString.Exclude
    private Symbol symbol;
}
