import { Injectable } from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {PlayerCardSimple} from '../models/interfaces/PlayerCard';

@Injectable({
  providedIn: 'root'
})
export class CardService {

  private apiUrl = 'http://localhost:8080/api/v1/cards';
  constructor(private http: HttpClient) {}

  getPlayerCards(playerGameId: number): Observable<PlayerCardSimple[]> {
    return this.http.get<PlayerCardSimple[]>(`${this.apiUrl}/player/${playerGameId}`);
  }
/*
  assignCardToPlayer(playerGameId: number, cardId: number): Observable<CountryCard> {
    return this.http.post<CountryCard>(
      `${this.apiUrl}/jugador/${playerGameId}/asignar?tarjetaId=${cardId}`, {}
    );
  }

  markCardAsUsed(tarjetaJugadorId: number): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/marcar-usada/${tarjetaJugadorId}`, {});
  }

  getAvailableCountryCard(): Observable<CountryCard> {
    return this.http.get<CountryCard>(`${this.apiUrl}/pais/disponible`);
  }
 */

  requestArmiesByCard(gameId: number, playerGameId: number, cardId: number): Observable<void> {
    const body = {
      gameId: gameId,
      playerGameId: playerGameId,
      cardId: cardId
    };
    return this.http.post<void>(`${this.apiUrl}/player/request-army`,body,{
      headers: { 'Content-Type': 'application/json' }
    });
  }

  doExchange(gameId: number, playerId: number, cardsIds: number[]): Observable<boolean> {
    const params = new HttpParams()
        .set('cardsIds', cardsIds.join(','))
        .set('gameId', gameId.toString());

    return this.http.post<boolean>(
        `${this.apiUrl}/player/${playerId}/exchange`,
        null, // cuerpo vacío
        { params }
    );
  }

  askCard(playerId: number) {
    return this.http.post(`${this.apiUrl}/player/${playerId}/request`, null, {
      responseType: 'text'
    });
  }

}
