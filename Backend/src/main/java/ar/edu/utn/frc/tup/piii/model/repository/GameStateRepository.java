package ar.edu.utn.frc.tup.piii.model.repository;

import ar.edu.utn.frc.tup.piii.model.entities.StateGameEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GameStateRepository extends JpaRepository<StateGameEntity,Integer> {
    StateGameEntity findByDescription(String state);

    Optional<StateGameEntity> findByDescriptionIgnoreCase(String name);
}
