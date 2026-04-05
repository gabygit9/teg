package ar.edu.utn.frc.tup.piii.model.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *  Representa el nivel de dificultad de un bot.
 * Niveles típicos:
 * - Novato
 * - Balanceado
 * - Experto
 *
 * @author GabrielaCamacho
 * @version 1.0
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Levels_bot")
public class LevelBot {
    public static final String NOVICE = "novice";
    public static final String BALANCED ="balanced";
    public static final String EXPERT = "expert";

    @Id
    @Column(name = "level_id")
    private int id;

    @Column(name = "name_bot")
    private String name;
}
