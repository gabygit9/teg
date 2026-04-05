package ar.edu.utn.frc.tup.piii.services.impl;

import ar.edu.utn.frc.tup.piii.model.entities.StateGameEntity;
import ar.edu.utn.frc.tup.piii.model.repository.GameStateRepository;
import ar.edu.utn.frc.tup.piii.services.interfaces.GameStateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GameStateServiceImpl implements GameStateService {

    private final GameStateRepository gameStateRepository;

    @Override
    public StateGameEntity findById(int id) {
        return gameStateRepository.findById(id).orElse(null);
    }

    @Override
    public List<StateGameEntity> findAll() {
        return gameStateRepository.findAll();
    }

    @Override
    public StateGameEntity findByDescription(String description) {
        return gameStateRepository.findByDescription(description);
    }

    @Override
    public Optional<StateGameEntity> findByDescriptionIgnoreCase(String description) {
        return gameStateRepository.findByDescriptionIgnoreCase(description);
    }


}
