package ar.edu.utn.frc.tup.piii.controllers;


import ar.edu.utn.frc.tup.piii.dto.MessageDTO;
import ar.edu.utn.frc.tup.piii.model.entities.Message;
import ar.edu.utn.frc.tup.piii.services.interfaces.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


//TODO agregar manejo de excepciones y respuestas HTTP más detalladas
//TODO usar responseEntity<?> en la firma de todos los métodos
/**
 * Controlador para gestionar los mensajes de chat dentro de una partida.
 * Permite enviar y obtener mensajes, y está vinculado al sistema de comunicación.
 *
 * @author GabrielaCamacho
 * @version 1.0
 *
 * @see Message
 * @see ChatService
 */
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    /**
     * Obtiene todos los mensajes de una partida específica.
     *
     * @param gameId ID de la partida
     * @return lista de mensajes
     */
    @GetMapping("/{gameId}/messages")
    public ResponseEntity<List<MessageDTO>> getMessagesFromGame(@PathVariable int gameId){
        try {
           List <MessageDTO> resMsg = chatService.getMessagesPerGame(gameId);
           return ResponseEntity.ok(resMsg);
        }catch (Exception ex){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    /**
     * Permite enviar un nuevo mensaje en una partida.
     *
     * @param request objeto mensaje con contenido, emisor y partida
     * @return mensaje guardado
     */
    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestBody MessageDTO request) {
        try {
            MessageDTO resMsg = chatService.sendMessage(request.getGameId(), request.getSenderId(), request.getContent());
            if (resMsg != null) {
                return ResponseEntity.ok(resMsg);
            } else {
                return ResponseEntity.badRequest().build();
            }
        }catch (Exception ex){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }



     /**
     * Modifica el contenido de un mensaje en una partida.
     * @param messageId mensaje que se va a actualizar
     * @return mensaje modificado y guardado
     */
    @PatchMapping("/{messageId}/modify")
    public ResponseEntity<MessageDTO> modifyMessage(@PathVariable int messageId, @RequestBody MessageDTO request) {
        try {
            MessageDTO resMsg = chatService.modifyMessage(request.getGameId(), request.getSenderId(), messageId, request.getContent());
            if (resMsg != null) {
                return ResponseEntity.ok(resMsg);
            } else {
                return ResponseEntity.badRequest().build();
            }
        }catch (Exception ex){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


     /**
     * Baja logica de un mensaje.
     *
     * @param gameId ID de la partida en juego
     * @param senderId  Jugador que envia el mensaje
     * @param messageId Mensaje que se va a eliminar
     * @return Devuelve true si la baja fue exitosa
     */
    @PatchMapping("/{gameId}/messages/{messageId}/remove/{senderId}")
    public ResponseEntity<?> removeMessage(@PathVariable int messageId, @PathVariable int gameId, @PathVariable int senderId) {
        try {
            boolean resDlt = chatService.remove(gameId, senderId, messageId);
            if (resDlt) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest().build();
            }
        }catch (Exception ex){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }

}
