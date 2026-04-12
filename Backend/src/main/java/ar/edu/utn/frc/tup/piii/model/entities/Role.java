package ar.edu.utn.frc.tup.piii.model.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *  Representa un rol específico que puede tener un usuario
 *  Un rol puede ser: "ADMINISTRADOR", "JUGADOR"
 *
 * @author GabrielaCamacho
 * @version 1.0
 *
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Rols")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name = "role_id")
    private Integer id;

    private String description;
}
