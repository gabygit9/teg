package ar.edu.utn.frc.tup.piii.model.repository;

import ar.edu.utn.frc.tup.piii.model.entities.CountryConnection;
import ar.edu.utn.frc.tup.piii.model.entities.CountryConnectionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio JPA para conexiones entre países (fronteras).
 * {@code @author:} GabrielaCamacho
 */
@Repository
public interface CountryConnectionRepository extends JpaRepository<CountryConnection, CountryConnectionId> {

    @Query("SELECT c FROM CountryConnection c WHERE" +
            "(c.countryOrigin.id = :origin AND c.countryDestination.id = :destination) OR" +
            "(c.countryOrigin.id = :destination AND c.countryDestination.id = :origin)")
    Optional<CountryConnection> existsConnection(@Param("origin") int origin, @Param("destination") int destination);

}
