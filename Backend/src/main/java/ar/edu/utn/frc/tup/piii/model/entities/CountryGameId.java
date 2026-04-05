package ar.edu.utn.frc.tup.piii.model.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CountryGameId implements Serializable {

    @Column(name = "country_id")
    private int countryId;

    @Column(name = "country_game_id")
    private int gameId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CountryGameId)) return false;
        CountryGameId that = (CountryGameId) o;
        return countryId == that.countryId && gameId == that.gameId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(countryId, gameId);
    }
}
