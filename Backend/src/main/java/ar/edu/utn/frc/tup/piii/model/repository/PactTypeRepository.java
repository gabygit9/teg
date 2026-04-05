package ar.edu.utn.frc.tup.piii.model.repository;

import ar.edu.utn.frc.tup.piii.model.entities.PactType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Esta interfaz es el repositorio de la clase Tipo de pacto, es donde se hace las consultas a la tabla Tipo_pactos en la BD.
 * {@code @author:} Franco Chachagua
 * @see PactType
 */
@Repository
public interface PactTypeRepository extends JpaRepository<PactType, Integer> {
    PactType findByDescription(String name);
}
