package ar.edu.utn.frc.tup.piii.services.interfaces;

import ar.edu.utn.frc.tup.piii.model.entities.StateGameEntity;

import java.util.List;
import java.util.Optional;

public interface GameStateService {

    StateGameEntity findById(int id);

    List<StateGameEntity> findAll();

    StateGameEntity findByDescription(String description);

    Optional<StateGameEntity> findByDescriptionIgnoreCase(String description);


}
