import {Injectable, signal} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Objective} from '../models/interfaces/Objective';
import {Observable, tap} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ObjectiveService {

  private apiUrl = 'http://localhost:8080/api/v1/objectives';
  constructor(private http: HttpClient) { }

  private _objective= signal<Objective | null>(null);
  private _achieved = signal<boolean>(false);

  get objective(){
    return this._objective;
  }

  get achieved(){
    return this._achieved;
  }

  getSecretObjective(gameId: number, playerGameId: number): Observable<Objective> {
    return this.http.get<Objective>(`${this.apiUrl}/${gameId}/player/${playerGameId}/objective`)
      .pipe(
        tap(data => this._objective.set(data))
      );
  }

}

