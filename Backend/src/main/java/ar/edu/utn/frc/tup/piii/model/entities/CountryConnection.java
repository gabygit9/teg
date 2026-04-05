package ar.edu.utn.frc.tup.piii.model.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Countries_connection")
public class CountryConnection {
    @EmbeddedId
    private CountryConnectionId id;

    @ManyToOne
    @MapsId("countryOriginId")
    @JoinColumn(name = "country_origin_id")
    private Country countryOrigin;

    @ManyToOne
    @MapsId("countryDestinationId")
    @JoinColumn(name = "country_destination_id")
    private Country countryDestination;
}
