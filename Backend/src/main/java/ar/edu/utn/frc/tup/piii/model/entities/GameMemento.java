package ar.edu.utn.frc.tup.piii.model.entities;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Representa un memento del estado de una partida en el juego TEG.
 * Esta clase almacena una instantánea serializada del estado completo de una partida en un momento dado,
 * permitiendo guardar y restaurar dicho estado para continuar la partida posteriormente.
 * Cada instancia incluye información sobre la versión del memento, la fecha y hora en que fue creado,
 * y está asociada a una partida específica.
 *
 *
 @see StateGameEntity
 */
@Entity
@Table(name = "Game_mementos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameMemento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "memento_id")
    private int mementoId;

    @Column(name = "date_time_memento", nullable = false)
    private LocalDateTime dateTime;

    @Lob
    @Column(name = "state_memento", columnDefinition = "TEXT", nullable = false)
    private String stateSerialized;

    @ManyToOne
    @JoinColumn(name = "game_id")
    private Game game;

    @Column(name = "game_version")
    private int version;

}
