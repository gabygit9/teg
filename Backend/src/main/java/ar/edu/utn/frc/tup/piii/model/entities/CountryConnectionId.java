package ar.edu.utn.frc.tup.piii.model.entities;

import jakarta.persistence.Embeddable;
import lombok.Data;
import java.io.Serializable;

@Data
@Embeddable
public class CountryConnectionId implements Serializable {

    private int countryOriginId;
    private int countryDestinationId;
}
