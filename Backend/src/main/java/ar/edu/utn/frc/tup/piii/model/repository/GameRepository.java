package ar.edu.utn.frc.tup.piii.model.repository;

import ar.edu.utn.frc.tup.piii.model.entities.StateGameEntity;
import ar.edu.utn.frc.tup.piii.model.entities.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para partidas del juego.
 * {@code @author:} GabrielaCamacho
 */
@Repository
public interface GameRepository extends JpaRepository<Game, Integer> {

    /**
     * Busca en la base de datos en la tabla Partida las partidas que tengan un estado
     * que no se encuentre el los que se pasan por parámetros.
     * @return List<Partida>
     */
    List<Game> findByStatesIn(List<StateGameEntity> states);

}
