package ar.edu.utn.frc.tup.piii.services.interfaces;

import ar.edu.utn.frc.tup.piii.dto.ResultAttackDto;
import ar.edu.utn.frc.tup.piii.model.entities.PlayerGame;
import ar.edu.utn.frc.tup.piii.model.entities.CountryGameId;

import java.util.List;

public interface CombatService {
    List<Integer> throwDice(int attackerId, int defenderId, int maxDice);
    void conquerCountry(int countryId, PlayerGame player);
    void announceAttack(CountryGameId attackerId, CountryGameId defenderId);
    ResultAttackDto attack(int gameId, int countryAttackerId, int countryDefenderId, int maxDice);
    void regroupArmy(int playerId, int originId, int destinationId, int amount);
}
