package ar.edu.utn.frc.tup.piii.model.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *  Representa un usuario registrado en la plataforma.
 *  El usuario puede jugar partidas, y se le asigna un rol para determinar sus permisos.
 *
 * @author GabrielaCamacho
 * @version 1.0
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name = "user_id")
    private int id;

    @Column (name = "user_name")
    private String name;

    private String email;

    private String password;

    @ManyToOne
    @JoinColumn(name = "user_rol_id", nullable = false)
    private Role role;
}
