import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {BehaviorSubject, Observable} from 'rxjs';
import {Message} from '../models/interfaces/Message';
import {Game} from '../models/interfaces/Game';


@Injectable({
  providedIn: 'root'
})
export class LobbyService {
  private apiUrlMessages = 'http://localhost:8080/api/v1/chat';
  private apiUrlGame = 'http://localhost:8080/api/v1/game';
  private messagesSubject = new BehaviorSubject<Message[]>([]);
  messages$ = this.messagesSubject.asObservable();

  constructor(private http: HttpClient) { }

  // Chat

  getMessagesByGame(gameId: number): Observable<Message[]> {
    return this.http.get<Message[]>(`${this.apiUrlMessages}/${gameId}/messages`);
  }

  sendMessage(message : Message): Observable<Message> {
    return this.http.post<Message>(`${this.apiUrlMessages}/send`, message);
  }

  loadMessages(gameId: number): void {
    this.getMessagesByGame(gameId).subscribe({
      next: (messages: any) => {
        this.messagesSubject.next(messages);
      },
      error: (error) => {
        console.error('Error loading the messages:', error);
      }
    });
  }

  // Game

  createGame(game: Game): Observable<boolean> {
    return this.http.post<boolean>(`${this.apiUrlGame}/play`, game);
  }

  loadGame(gameId: number): Observable<boolean> {
    return this.http.put<boolean>(`${this.apiUrlGame}/load/${gameId}`, { });
  }

  getGameById(gameId: number): Observable<Game> {
    return this.http.get<Game>(`${this.apiUrlGame}/${gameId}`);
  }

}
