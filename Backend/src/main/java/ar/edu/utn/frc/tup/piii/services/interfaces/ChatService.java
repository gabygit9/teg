package ar.edu.utn.frc.tup.piii.services.interfaces;

import ar.edu.utn.frc.tup.piii.dto.MessageDTO;

import java.util.List;

public interface ChatService {
    /**
     * Enviar un mensaje en una partida.
     *
     * @param senderId Jugador que envía el mensaje.
     * @param content Texto del mensaje.
     * @param gameId Partida en la que se envía.
     * @return Mensaje creado y guardado.
     */
    MessageDTO sendMessage(int gameId, int senderId, String content);


    /**
     * Obtener todos los mensajes de una partida.
     *
     * @param gameId Partida de la que se desea obtener los mensajes.
     * @return Lista de mensajes ordenados cronológicamente.
     */
    List<MessageDTO> getMessagesPerGame(int gameId);


    /**
     * Realizar baja logica de un mensaje del chat.
     *
     * @param gameId Partida de la que se desea realizar la baja logica.
     * @param senderId Jugador que envia el mensaje.
     * @param messageId Mensaje que se envia.
     * @return Devuelve true si el mensaje fue eliminado con exito.
     */
    boolean remove(int gameId, int senderId, int messageId);


    /**
     * Modificar el content de un mensaje del chat.
     *
     * @param gameId Partida de la que se desea realizar la modificacion.
     * @param senderId Jugador que envia el mensaje.
     * @param messageId Mensaje que se envia.
     * @param content Contenido del mensaje.
     * @return Mensaje modificado y guardado.
     */
    MessageDTO modifyMessage(int gameId, int senderId, int messageId, String content);

}
