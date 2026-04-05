import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { GameMemento } from '../models/interfaces/GameMemento';
import {SavedGames} from '../models/interfaces/SavedGames';

@Injectable({
  providedIn: 'root'
})
export class MementoService {
  private baseUrl = 'http://localhost:8080/api/v1/mementos';

  constructor(private http: HttpClient) {}

  saveMemento(gameId: number, version: number = 1): Observable<GameMemento> {
    return this.http.post<GameMemento>(
      `${this.baseUrl}/game/${gameId}/save?version=${version}`,
      null
    );
  }

  getMementos(gameId: number): Observable<GameMemento[]> {
    return this.http.get<GameMemento[]>(`${this.baseUrl}/game/${gameId}`);
  }

  getLastState(gameId: number): Observable<GameMemento> {
    return this.http.get<GameMemento>(`${this.baseUrl}/game/${gameId}/last`);
  }

  restoreMemento(mementoId: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/restore/${mementoId}`, null);
  }

  getSavedGames(): Observable<SavedGames[]> {
    return this.http.get<SavedGames[]>('http://localhost:8080/api/v1/mementos');
  }






}
