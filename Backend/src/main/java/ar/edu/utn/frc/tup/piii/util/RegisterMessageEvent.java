package ar.edu.utn.frc.tup.piii.util;
import ar.edu.utn.frc.tup.piii.dto.RegisterMessageEventDTO;
import ar.edu.utn.frc.tup.piii.model.entities.*;
import ar.edu.utn.frc.tup.piii.model.enums.TurnPhase;
import org.springframework.stereotype.Component;

/**
 * Clase encargada de construir mensajes para registrar en el historial de eventos del juego
 */
@Component
public class RegisterMessageEvent {


    public String moveArmiesRegistry(RegisterMessageEventDTO dto) {
        return "El jugador " + dto.getOriginCountry().getPlayerGame().getPlayer().getName()
                + " movió " + dto.getAmountTroops() + " tropas desde "
                + dto.getOriginCountry().getCountry().getName() + " hacia " + dto.getDestinationCountry().getCountry().getName()
                + " para reagrupar fuerzas.";
    }

    public String conquerCountryRegistry(RegisterMessageEventDTO dto) {
        return "El jugador " + dto.getOriginCountry().getPlayerGame().getPlayer().getName()
                + " ha conquistado " + dto.getDestinationCountry().getCountry().getName() + " perteneciente al jugador "
                + dto.getDestinationCountry().getPlayerGame().getPlayer().getName();
    }


    public String attackArmiesRegistry(RegisterMessageEventDTO dto) {
        return "El jugador " + dto.getOriginCountry().getPlayerGame().getPlayer().getName()
                + " ha atacado al jugador " + dto.getDestinationCountry().getPlayerGame().getPlayer().getName()
                + " con " + dto.getAmountTroops() + " tropas, desde " + dto.getOriginCountry().getCountry().getName()
                + " a " + dto.getDestinationCountry().getCountry().getName();
    }

    public String startGameRegistry(Game game) {
        return "La partida nro°" + game.getId() + " ha sido iniciada.";
    }

    public String continueGameRegistry(Game game) {
        return "La partida nro°" + game.getId() + " ha sido reanudada.";
    }

    public String finishGameRegistry(Game game) {
        return "La partida nro°" + game.getId() + " ha sido finalizada.";
    }

    public String startHostilitiesGameRegistry(int gameId) {
        return "La partida nro°" + gameId + " ha entrado en la fase de hostilidades.";
    }

    public String moveStateGameRegistry(int gameId) {
        return "La partida nro°" + gameId + " avanzó al siguiente estado.";
    }


    public String exchangeCardsRegister(PlayerGame player, int quantityArmies) {
        return "El player " + player.getPlayer().getName()
                + " ha canjeado tarjetas y recibió " + quantityArmies + " ejércitos.";
    }

    // no estoy seguro que se muestre bien
    public String changeStateGameRegistry(Game game, StateGameEntity newState) {
        return "La partida nro°" + game.getId() + " cambió su estado a '" + newState.getDescription() + "'.";
    }

    public static String increaseArmies(BasePlayer player, String country, int quantity) {
        return "El player '" + player.getName() + "' agregó " + quantity + " ejércitos al país '" + country + "'";
    }



    public static String receiveCard(BasePlayer jugador, String nombrePais) {
        return "El jugador " + jugador.getName() + " recibió una tarjeta del país " + nombrePais + ".";
    }

    public static String giveArmiesPerCard(BasePlayer jugador, String nombrePais) {
        return "El jugador " + jugador.getName() + " obtuvo 2 ejércitos adicionales por poseer el país " + nombrePais + " al usar una tarjeta.";
    }

    public static String changePhase(BasePlayer jugador, TurnPhase nuevaFase) {
        return "El jugador " + jugador.getName() + " pasó a la fase " + nuevaFase.name();
    }

    public static String putArmy(BasePlayer jugador, Country country, int cantidad) {
        return "El jugador " + jugador.getName() + " colocó " + cantidad + " ejército(s) en " + country.getName() + ".";
    }

    public static String startTurn(BasePlayer jugador) {
        return "El jugador " + jugador.getName() + " ha iniciado su turno.";
    }








    // ejemplo de uso dentro del método de una accion:
    //        String mensaje = finalizarPartidaRegistro(partida);
    //        registrarEvento(partida, mensaje);


    // en proceso...



}
