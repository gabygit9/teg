package ar.edu.utn.frc.tup.piii.model.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 *  Representa la ocupación y estado de un país en una partida específica.
 *  Indica qué jugador controla el país y cuántos ejércitos hay en él.
 *
 * @author GabrielaCamacho
 * @version 1.0
 *
 * @see Country
 * @see Game
 * @see BasePlayer
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Countries_game")
public class CountryGame {

    @EmbeddedId
    private CountryGameId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("countryId")
    @JoinColumn(name = "country_id", nullable = false)
    @ToString.Exclude
    private Country country;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("gameId")
    @JoinColumn(name = "country_game_id", nullable = false)
    @ToString.Exclude
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_player_id", nullable = false)
    @ToString.Exclude
    private PlayerGame playerGame;

    @Column(name = "amount_armies")
    private int amountArmies;

    public CountryGame(Country country, Game game, PlayerGame playerGame, int amountArmies) {
        this.id = new CountryGameId(country.getId(), game.getId());
        this.country = country;
        this.game = game;
        this.playerGame = playerGame;
        this.amountArmies = amountArmies;
    }
}
