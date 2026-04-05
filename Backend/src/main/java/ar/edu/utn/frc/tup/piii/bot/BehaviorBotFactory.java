package ar.edu.utn.frc.tup.piii.bot;

import ar.edu.utn.frc.tup.piii.model.entities.LevelBot;
import org.springframework.context.ApplicationContext;

/**
 * Fábrica que retorna el comportamiento de IA correspondiente a cada nivel de bot.
 * Utiliza el nombre del nivel para instanciar la estrategia adecuada.
 *
 * Aplica el patrón de diseño Strategy.
 *
 * @author GabrielaCamacho
 * @version 1.0
 * @see IBehaviorBot
 * @see LevelBot
 */
public class BehaviorBotFactory {
    private static ApplicationContext context;

    public static void setApplicationContext(ApplicationContext ctx) {
        context = ctx;
    }
    public static IBehaviorBot getBehavior(LevelBot level){

        String name = level.getName();
        return switch (name){
            case LevelBot.NOVICE -> context.getBean(BotNoviceStrategy.class);
            case LevelBot.BALANCED -> context.getBean(BotBalancedStrategy.class);
            case LevelBot.EXPERT -> new BotExpertStrategy();
            default -> throw new IllegalArgumentException("Nivel de bot desconocido "+ name);
        };
    }
}


//Service o turno: jugadorBot.setComportamiento(ComportamientoBotFactory.obtenerComportamiento(jugadorBot.getNivelBot()));
//JugadorBot bot = jugadorBotRepository.findById(id).get();
//bot.setComportamiento(ComportamientoBotFactory.obtenerComportamiento(bot.getNivelBot()));