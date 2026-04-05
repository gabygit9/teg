import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {EventHistory} from '../models/interfaces/EventHistory';

@Injectable({
  providedIn: 'root'
})
export class HistoryService {

  private apiUrl = 'http://localhost:8080/api/v1/history';
  constructor(private http: HttpClient) {}


  getHistory(game: number): Observable<EventHistory[]> {
    return this.http.get<EventHistory[]>(`${this.apiUrl}/${game}`);
  }

}

