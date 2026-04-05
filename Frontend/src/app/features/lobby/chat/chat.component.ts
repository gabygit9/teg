import { Component, OnInit, OnDestroy, ViewChild, ElementRef } from '@angular/core';
import { LobbyService } from '../../../core/services/lobby.service';
import { Subscription } from 'rxjs';
import { Message } from '../../../core/models/interfaces/Message';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-chat',
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.css'],
  imports: [CommonModule, FormsModule],
  standalone: true
})
export class ChatComponent implements OnInit, OnDestroy {
  @ViewChild('messagesContainer') messagesContainer!: ElementRef;
  newMessage: string = '';
  messages: Message[] = [];

  players: any[] = [];
  gameId: number | null = null;

  playerId: number | null = null;
  name: string = '';
  color: string = '';

  openChat = true;

  private messagesSub!: Subscription;
  private intervalId: any;

  constructor(
    private lobbyService: LobbyService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadPlayer();
    if (this.gameId != null) {
      this.lobbyService.loadMessages(this.gameId);

      this.messagesSub = this.lobbyService.messages$.subscribe({
        next: (msgs) => {
          this.messages = msgs;
          setTimeout(() => this.scrollToBottom(), 0);
        }
      })

      this.intervalId = setInterval(() => {
        this.lobbyService.loadMessages(this.gameId!);
      }, 3000);
    }
  }

  loadPlayer(): void {
    const data = sessionStorage.getItem("gameData");
    const userName = this.authService.getUserNameFromToken();
    if (!data || !userName) {
      return;
    }
    try {
      const parsed = JSON.parse(data);
      this.players = parsed.players || [];
      this.gameId = parsed.game.gameId ?? null;

      const player = this.players.find(j => j.player?.name === userName);

      if (player) {
        this.playerId = player.player.id;
        this.name = player.player.name;
        this.color = player.color;

      } else {
        console.log('No se encontro el jugador:', userName);
      }
    } catch (e) {
      console.error('Error al parsear gameData', e);
    }
  }

  ngOnDestroy(): void {
    if (this.messagesSub) this.messagesSub.unsubscribe();
    if (this.intervalId) clearInterval(this.intervalId);
  }

  sendMessage(): void {
    if (this.newMessage.trim() && this.playerId != null && this.gameId != null) {
      const message: Message = {
        id: 0,
        gameId: this.gameId,
        senderId: this.playerId,
        content: this.newMessage,
        isActive: true,
        isEdited: false,
        dateTime: new Date().toISOString()
      };
      this.lobbyService.sendMessage(message).subscribe({
        next: () => {
          this.lobbyService.loadMessages(this.gameId!);
          this.newMessage = '';
        },
        error: err => console.error('Error al enviar mensaje', err)
      });
    }
  }


  getHour(isoDate: string): string {
    const utc = new Date(isoDate);
    const argentineDate = new Date(utc.getTime() + 3 * 60 * 60 * 1000); // le sumo 3 horas xd
    return argentineDate.toLocaleTimeString('es-AR', {
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  getPlayerName(id: number): string {
    const player = this.players.find(j => j.player.id === id);
    return player?.name || 'Jugador';
  }

  //Cuando obtiene los msg baja el scroll
  scrollToBottom(): void {
    try {
      if (this.messagesContainer && this.messagesContainer.nativeElement) {
        this.messagesContainer.nativeElement.scrollTop = this.messagesContainer.nativeElement.scrollHeight;
      }
    } catch (err) {
      console.error('Error al hacer scroll', err);
    }
  }

  private mapColor(colorEsp: string): string {
    const map: Record<string, string> = {
      //la misma que en player-component
      red: 'red',
      blue: 'blue',
      green: 'green',
      yellow: 'yellow',
      black: 'black',
      magenta: 'magenta'
    };
    return map[colorEsp.toLowerCase()];
  }

  getColorPlayers(id: number): string {
    const player = this.players.find(j => j.player.id === id);
    const colorEsp = player?.color || 'gray';
    return this.mapColor(colorEsp);
  }

  closeChat(): void {
    this.openChat = false;
  }
}

//CHAT EN TIEMPO REAL ENTRE LOS JUGADORES
