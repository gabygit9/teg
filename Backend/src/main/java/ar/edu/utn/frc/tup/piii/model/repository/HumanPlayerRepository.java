package ar.edu.utn.frc.tup.piii.model.repository;

import ar.edu.utn.frc.tup.piii.model.entities.HumanPlayer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HumanPlayerRepository extends JpaRepository<HumanPlayer, Integer> {
    HumanPlayer findByUser_Id(int userId);
}
