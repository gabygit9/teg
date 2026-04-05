package ar.edu.utn.frc.tup.piii.model.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 *  Representa un pacto entre jugadores durante una partida.
 *  Puede establecer condiciones como no agresión, zonas protegidas, etc.
 *
 * @author GabrielaCamacho
 * @version 1.0
 *
 * @see BasePlayer
 * @see Game
 * @see PactType
 * @see Country
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Pacts")
public class Pact {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pact_id")
    private int id;

    @ManyToOne
    @JoinColumn(name = "game_pact_id")
    private Game game;

    @ManyToOne
    @JoinColumn(name = "pact_type_id")
    private PactType pactType;

    @ManyToMany
    @JoinTable(
            name = "Players_pact",
            joinColumns = @JoinColumn(name = "pact_id"),
            inverseJoinColumns = @JoinColumn(name = "base_player_id")
    )
    private List<BasePlayer> players;

    @ManyToMany
    @JoinTable(
            name = "Countries_pact",
            joinColumns = @JoinColumn(name = "pact_id"),
            inverseJoinColumns = @JoinColumn(name = "country_id")
    )
    private List<Country> countries;


    @ManyToOne
    @JoinColumn(name = "creator_player_id", nullable = false)
    private BasePlayer playerCreated;

    private boolean active;

    @Column(name = "date_time_pact", nullable = false)
    private LocalDateTime dateTime;

    public void brokePact() {
        this.active = false;
    }

}
