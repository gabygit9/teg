package ar.edu.utn.frc.tup.piii.model.repository;

import ar.edu.utn.frc.tup.piii.model.entities.BasePlayer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Esta interfaz es el repositorio de la clase Jugador, es donde se hacen las consultas a la tabla jugador en la BD.
 * {@code @author:} Ismael Ceballos
 * @see BasePlayer
 */
@Repository
public interface PlayerRepository extends JpaRepository<BasePlayer, Integer> {
    @Query("SELECT j FROM BasePlayer j")
    List<BasePlayer> findAllPlayers();

    BasePlayer findByName(String name);
}
