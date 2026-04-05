package ar.edu.utn.frc.tup.piii.util;

import ar.edu.utn.frc.tup.piii.model.entities.PlayerGame;
import ar.edu.utn.frc.tup.piii.model.entities.CountryGame;
import ar.edu.utn.frc.tup.piii.services.interfaces.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class CombatUtil {
    @Autowired
    private CombatService combatServiceBean;

    @Autowired
    private TurnService turnServiceBean;

    @Autowired
    private CountryGameService countryGameServiceBean;

    public static CombatService combatService;
    public static TurnService turnService;
    public static CountryGameService countryGameService;

    @PostConstruct
    public void init() {
        combatService = combatServiceBean;
        turnService = turnServiceBean;
        countryGameService = countryGameServiceBean;
    }

    /**
     * Ejecuta una batalla entre dos países siguiendo las reglas del juego TEG.
     * Simula la tirada de dados, calcula pérdidas y realiza la conquista si el defender pierde todas las tropas.
     * También mueve tropas mínimas al país conquistado y marca si hubo conquista.
     *
     * @param attacker         el país que ataca
     * @param defender         el país que recibe el ataque
     * @param player  el player que está atacando (bot)
     * @param gameService   servicio de partida para aplicar cambios reales en el juego
     * @return true si el país fue conquistado, false si no
     */
    public static boolean resolveCombat(CountryGame attacker, CountryGame defender, PlayerGame player, GameService gameService) {
        int attackerTroops = attacker.getAmountArmies();
        int defenderTroops = defender.getAmountArmies();

        if(attackerTroops<2 || defenderTroops<1) return false;

        int attackerDice = Math.min(3, attackerTroops - 1);
        int defenderDice = Math.min(3, defenderTroops);

        List<Integer> diceA = new ArrayList<>(combatService.throwDice(attacker.getPlayerGame().getPlayer().getId(), defender.getPlayerGame().getPlayer().getId(), attackerDice));
        List<Integer> diceD = new ArrayList<>(combatService.throwDice(defender.getPlayerGame().getPlayer().getId(), attacker.getPlayerGame().getPlayer().getId(), defenderDice));

        diceA.sort(Comparator.reverseOrder());
        diceD.sort(Comparator.reverseOrder());

        int loseA = 0;
        int loseD = 0;

        for (int i = 0; i < Math.min(diceA.size(), diceD.size()); i++) {
            if (diceA.get(i) > diceD.get(i)) loseD++;
            else loseA++;
        }

        //actualizar tropas localmente
        attacker.setAmountArmies(attacker.getAmountArmies() - loseA);
        countryGameService.save(attacker);
        defender.setAmountArmies(defender.getAmountArmies() - loseD);
        countryGameService.save(defender);

        // Si defender quedó sin tropas, conquista
        if (defender.getAmountArmies() <= 0) {

            attacker.setAmountArmies(attacker.getAmountArmies() + 1);
            countryGameService.save(attacker);

            // Mover mínimo 1 tropa al país conquistado
            int move = Math.min(attacker.getAmountArmies() - 1, 3);
            if(move <= 0) return false;
            //todo validar
            combatService.conquerCountry(defender.getCountry().getId(), player);
            turnService.moveArmies(attacker.getId(), defender.getId(), move);

            return true;
        }
        return false;
    }

}
