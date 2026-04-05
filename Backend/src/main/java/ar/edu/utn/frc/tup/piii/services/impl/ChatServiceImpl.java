package ar.edu.utn.frc.tup.piii.services.impl;

import ar.edu.utn.frc.tup.piii.dto.BasePlayerDTO;
import ar.edu.utn.frc.tup.piii.dto.MessageDTO;
import ar.edu.utn.frc.tup.piii.model.entities.Game;
import ar.edu.utn.frc.tup.piii.model.entities.HumanPlayer;
import ar.edu.utn.frc.tup.piii.model.entities.Message;
import ar.edu.utn.frc.tup.piii.model.repository.MessageRepository;
import ar.edu.utn.frc.tup.piii.services.interfaces.ChatService;
import ar.edu.utn.frc.tup.piii.services.interfaces.PlayerService;
import ar.edu.utn.frc.tup.piii.services.interfaces.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación de la lógica de envío y recuperación de mensajes entre jugadores en una partida.
 * Esta clase maneja la persistencia de mensajes en el historial de chat.
 *
 * @author GabrielaCamacho
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService{
    private final MessageRepository messageRepository;
    private final PlayerService playerService;
    @Lazy
    private final GameService gameService;


    /**
     * Envía un nuevo mensaje desde un jugador a una partida.
     *
     * Crea una instancia de Mensaje con la información recibida,
     * le asigna la hora actual y lo guarda en la base de datos.
     *
     * @param senderId Jugador que envía el mensaje.
     * @param content Texto del mensaje.
     * @param gameId Partida a la que pertenece el mensaje.
     * @return El mensaje persistido con su ID generado y fecha asignada.
     */
    @Override
    public MessageDTO sendMessage(int gameId, int senderId, String content){

        Game game = gameService.findById(gameId);
        if (game == null)  return null;

        Optional<BasePlayerDTO> basePlayerDTO = playerService.findById(senderId);
        if (basePlayerDTO.isEmpty()) {
            return null;
        }
        HumanPlayer sender = new HumanPlayer();
        sender.setId(senderId);


        Message message = new Message();
        message.setGame(game);
        message.setSender(sender);
        message.setContent(content);
        message.setActiveState(true);
        message.setModified(false);
        message.setDatetime(LocalDateTime.now());

        Message msgRsp =  messageRepository.save(message);

        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setId(msgRsp.getId());
        messageDTO.setGameId(msgRsp.getGame().getId());
        messageDTO.setSenderId(msgRsp.getSender().getId());
        messageDTO.setContent(msgRsp.getContent());
        messageDTO.setStateActive(msgRsp.isActiveState());
        messageDTO.setModified(msgRsp.isModified());
        messageDTO.setDateTime(msgRsp.getDatetime());

        return messageDTO;
    }


    /**
     * Recupera todos los mensajes de una partida ordenados cronológicamente.
     *
     * Utiliza el repositorio para buscar por partida y orden ascendente
     * por fecha y hora.
     *
     * @param gameId La partida cuyo historial de mensajes se desea obtener.
     * @return Lista ordenada de mensajes asociados a la partida.
     */
    @Override
    public List<MessageDTO> getMessagesPerGame(int gameId){
        Game game = gameService.findById(gameId);
        if (game == null) return List.of();

        List<Message> messages = messageRepository.findByGameIdOrderByHourAsc(gameId);
        List<MessageDTO> msgResponse = new ArrayList<>();

        for (Message message : messages) {
            MessageDTO dto = new MessageDTO();

            dto.setId(message.getId());
            dto.setGameId(message.getGame().getId());
            dto.setSenderId(message.getSender().getId());
            dto.setContent(message.getContent());
            dto.setStateActive(message.isActiveState());
            dto.setModified(message.isModified());
            dto.setDateTime(message.getDatetime());

            msgResponse.add(dto);
        }

        return msgResponse;
    }



    /**
     * Actualiza la propiedad estadoActivo de un mensaje para darlo de baja
     *
     * @param gameId Id de la partida
     * @param senderId Jugador que envia el mensaje
     * @param messageId Id del mensaje
     * @return Devuelve true si la baja logica es exitosa.
     */
    @Override
    public boolean remove(int gameId, int senderId, int messageId) {
        Message message = messageRepository.findById(messageId).orElse(null);
        if (message == null) return false;
        if (message.getSender().getId() != senderId) return false;
        if (!message.isActiveState()) return false;

        message.setActiveState(false);
        messageRepository.save(message);

        return true;
    }



    /**
     * @param gameId Id de la partida
     * @param senderId Jugador que envia el mensaje
     * @param messageId Id del mensaje
     * @param content Texto del mensaje que se quiere editar
     * @return El mensaje guardado
     */
    @Override
    public MessageDTO modifyMessage(int gameId, int senderId, int messageId, String content) {
        Message message = messageRepository.findById(messageId).orElse(null);
        if (message == null) return null;
        if (message.getGame().getId() != gameId || message.getSender().getId() != senderId) return null;

        message.setContent(content);
        message.setModified(true);
        message.setDatetime(LocalDateTime.now());

        Message msgSaved = messageRepository.save(message);

        MessageDTO dto = new MessageDTO();
        dto.setId(msgSaved.getId());
        dto.setContent(msgSaved.getContent());
        dto.setSenderId(msgSaved.getSender().getId());
        dto.setGameId(msgSaved.getGame().getId());
        dto.setModified(msgSaved.isModified());
        dto.setDateTime(msgSaved.getDatetime());

        return dto;
    }
}
