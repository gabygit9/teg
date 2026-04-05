import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable, of} from 'rxjs';
import {PlayerGame} from '../models/interfaces/PlayerGame';
import {CountryGameDTO} from '../models/interfaces/CountryGame';
import {BasePlayerDTO, PlayerGameDto} from '../models/interfaces/GameDataDTO';
import {Player} from '../models/interfaces/Player';

@Injectable({
  providedIn: 'root'
})
export class PlayerService {
  private apiUrl = "http://localhost:8080/api/v1"; // agregar /jugadores
  constructor(private http: HttpClient) { }

  getCurrentPlayerByGame(gameId: number) : Observable<PlayerGame>{
    return this.http.get<PlayerGame>(`${this.apiUrl}/games/${gameId}/player-in-turn`);
  }

  getPlayerInGame(gameId: number, basePlayerId: number): Observable<PlayerGame> {
    return this.http.get<PlayerGame>(`${this.apiUrl}/games/${gameId}/player/${basePlayerId}`)
  }

  getConquerCountries(gameId: number, playerId: number): Observable<CountryGameDTO[]>{
    return this.http.get<CountryGameDTO[]>(`${this.apiUrl}/country-game/${gameId}/player/${playerId}`)
  }

  getBasePlayerByUserId(userId: number) {
    return this.http.get<Player>(`${this.apiUrl}/players/base-per-user/${userId}`);
  }

}

//INFO DEL PLAYER , PERFIL, ESTADISTICAS (SI LLEGAMOS)
