import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {CountryGame} from '../models/interfaces/CountryGame';
import {Subject, tap} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class MapService {
  private countryGameUrl = "http://localhost:8080/api/v1/game-countries";

  private refreshCountry$ = new Subject<void>();
  countryObservable = this.refreshCountry$.asObservable();

  constructor(private http: HttpClient) { }

  getCountryGame(gameId: number, gamePlayerId: number){
    return this.http.get<CountryGame>(this.countryGameUrl+
      "/by-player?partidaId="+gameId+"?gamePlayerId="+gamePlayerId);
  }

  postIncrementCountryArmy(countryId: number, gameId: number, amount: number){
    this.http.post(this.countryGameUrl+"/strengthen?countryId="
      +countryId+"?gameId="+gameId+"?amount="+amount,null).pipe(
      tap(() => this.refreshCountry$.next())
    );
  }

  postDecrementCountryArmy(countryId: number, gameId: number, amount: number){
    this.http.post(this.countryGameUrl+"/reduce?countryId="
      +countryId+"?gameId="+gameId+"?amount="+amount, null).pipe(
      tap(() => this.refreshCountry$.next())
    );
  }

  getAllCountryGame(gameId: number){
    return this.http.get<CountryGame[]>(this.countryGameUrl+
      "/by-game?gameId="+gameId);
  }

}
