package ar.edu.utn.frc.tup.piii.services.interfaces;

import ar.edu.utn.frc.tup.piii.dto.mementosDTOs.GameStateMementoDTO;
import ar.edu.utn.frc.tup.piii.dto.SaveGameRequestDTO;
import ar.edu.utn.frc.tup.piii.model.entities.Game;
import ar.edu.utn.frc.tup.piii.model.entities.GameMemento;

import java.util.List;

public interface GameMementoService {

    List<GameMemento> getStatesByGame(int gameId);

    GameMemento getLastState(int gameId);

    GameMemento saveMementoComplete(Game game, int version);

    GameStateMementoDTO restoreAndPersistState(int mementoId);

    List<SaveGameRequestDTO> listSaveGames();



}
