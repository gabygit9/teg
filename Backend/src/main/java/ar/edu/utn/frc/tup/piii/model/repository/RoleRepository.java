package ar.edu.utn.frc.tup.piii.model.repository;

import ar.edu.utn.frc.tup.piii.model.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para roles de usuario
 * {@code @author:} GabrielaCamacho
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
}
