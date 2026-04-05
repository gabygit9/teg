package ar.edu.utn.frc.tup.piii.model.repository;

import ar.edu.utn.frc.tup.piii.model.entities.CardPlayer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para tarjetas de jugador.
 * {@code @author:} GabrielaCamacho
 */
@Repository
public interface CardPlayerRepository extends JpaRepository<CardPlayer, Integer> {
    @Query("SELECT t FROM CardPlayer  t WHERE t.playerGame.id = :playerGameId")
    List<CardPlayer> findByPlayerGame_Id(int playerGameId);
}
