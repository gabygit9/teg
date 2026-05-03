package ar.edu.utn.frc.tup.piii.config;

import ar.edu.utn.frc.tup.piii.model.entities.CommunicationType;
import ar.edu.utn.frc.tup.piii.model.entities.LevelBot;
import ar.edu.utn.frc.tup.piii.model.repository.CommunicationTypeRepository;
import ar.edu.utn.frc.tup.piii.model.repository.LevelBotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Inicializa datos mínimos en la base de datos si no existen.
 * Crea communication types y niveles de bot por defecto.
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CommunicationTypeRepository communicationTypeRepository;
    private final LevelBotRepository levelBotRepository;

    @Override
    public void run(String... args) {
        if (communicationTypeRepository.count() == 0) {
            CommunicationType ct = new CommunicationType();
            ct.setId(1);
            ct.setDescription("CHAT");
            communicationTypeRepository.save(ct);
            System.out.println("[DataInitializer] Created default CommunicationType id=1 description=CHAT");
        }

        if (levelBotRepository.count() == 0) {
            LevelBot l1 = new LevelBot(1, "novice");
            LevelBot l2 = new LevelBot(2, "balanced");
            LevelBot l3 = new LevelBot(3, "expert");
            levelBotRepository.save(l1);
            levelBotRepository.save(l2);
            levelBotRepository.save(l3);
            System.out.println("[DataInitializer] Created default LevelBot entries (1..3)");
        }
   }
}
