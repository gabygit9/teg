import { Injectable } from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Game, GameDTO} from '../models/interfaces/Game';
import {Observable} from 'rxjs';
import {GameDataDTO} from '../models/interfaces/GameDataDTO';
import {AttackDto, AttackResultDto} from "../models/interfaces/AttackDto";
import {CountryGameDTO} from '../models/interfaces/CountryGame';
import {EndTurnDTO} from '../models/interfaces/EndTurnDTO';
import {PlacementDTO} from '../models/interfaces/PlacementDTO';
import {ArmyRegroupDTO} from '../models/interfaces/ArmyRegroupDTO';

@Injectable({
  providedIn: 'root'
})
export class GameService {

  private apiUrl = 'http://localhost:8080/api/v1';
  constructor(private http: HttpClient) {}

  doAttack(dto: AttackDto): Observable<AttackResultDto> {
    return this.http.post<AttackResultDto>(`${this.apiUrl}/turns/attack`, dto);
  }


  getAvailableActions(turnId: number) {
    return this.http.get<string[]>(`${this.apiUrl}/turns/${turnId}/available-actions`);
  }


    gameStart(game: any) {
    return this.http.post<GameDTO>(`${this.apiUrl}/games/init`, game);
  }

  gameContinue(id: number): Observable<boolean> {
    return this.http.put<boolean>(`${this.apiUrl}/games/continue/${id}`, {});
  }

  gameFinish(id: number): Observable<boolean> {
    return this.http.put<boolean>(`${this.apiUrl}/games/finish/${id}`, {});
  }

  getAllGames(): Observable<Game[]> {
    return this.http.get<Game[]>(this.apiUrl);
  }

  getGameById(id: number): Observable<Game> {
    return this.http.get<Game>(`${this.apiUrl}/games/${id}`);
  }

  saveState(id: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/games/save/${id}`, {});
  }

  loadState(id: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/games/load/${id}`, {});
  }

  dealCards(id: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/games/${id}/distribute-cards`, {});
  }

  rollDice(countryAttackerId: number, countryDefensorId: number, maxDice: number): Observable<number[]> {
    return this.http.get<number[]>(`${this.apiUrl}/games/throw-dice`, {
      params: {
        countryAttackerId: countryAttackerId.toString(),
        countryDefensorId: countryDefensorId.toString(),
        maxDice: maxDice.toString()
      }
    });
  }

  isBorderline(country1: number, country2: number): Observable<boolean> {
    return this.http.get<boolean>(`${this.apiUrl}/games/is-bordering`, {
      params: {
        country1: country1.toString(),
        country2: country2.toString()
      }
    });
  }

  retreatArmy(countryId: number, quantity: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/games/${countryId}/remove-armies`, {}, {
      params: { quantity: quantity.toString() }
    });
  }

  countryConquest(playerId: number, countryId: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/games/conquer-country`, {}, {
      params: {
        playerId: playerId.toString(),
        countryId: countryId.toString()
      }
    });
  }

  incorporateArmies(dto: PlacementDTO): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/games/turn/locate`, dto);
  }


  occupiedContinent (id: number): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/games/${id}/continent-occupied`);
  }

  getPacts(id: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/games/${id}/see-pacts`);
  }

  communicationStyle(id: number): Observable<string> {
    return this.http.get<string>(`${this.apiUrl}/games/${id}/communication`);
  }

  startHostilities(id: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/games/${id}/init-hostilities`, {});
  }

  announceAttack (originId: number, destineId: number) {
    return this.http.post<any>(`${this.apiUrl}/games/announce-attack`, {}, {
      params: {
        originId: originId.toString(),
        destineId: destineId.toString()
      }
    });
  }

  assignHumanInGame(gameId: number, name: string, colorId: number) {
    return this.http.post<any>(`http://localhost:8080/api/v1/players/player-game-human/${gameId}?name=${name}&colorId=${colorId}`, {});
  }

  assignBotInGame(gameId: number, difficultyId: number, colorId: number) {
    return this.http.post<any>(`http://localhost:8080/api/v1/players/player-game-bot/${gameId}?difficultyId=${difficultyId}&colorId=${colorId}`, {});
  }

  gameNextState(gameId: number) {
    return this.http.post<any>(`http://localhost:8080/api/v1/games/${gameId}/move-state`, {});
  }

  getGameData(gameId: number) {
    return this.http.get<GameDataDTO>(this.apiUrl+"/games/data-game/"+gameId);
  }

  getCountriesByPlayers(gameId: number, playerId: number):Observable<CountryGameDTO[]> {
    return this.http.get<CountryGameDTO[]>(`${this.apiUrl}/country-game/${gameId}/player/${playerId}`);
  }

  getAllCountriesOfTheGame(gameId: number):Observable<CountryGameDTO[]> {
    return this.http.get<CountryGameDTO[]>(`${this.apiUrl}/country-game/${gameId}`);
  }

  postEndTurn(body: EndTurnDTO) {
    return this.http.post(`${this.apiUrl}/games/turn/finish`, body)
  }

  armyRegroup(dto: ArmyRegroupDTO) {
    return this.http.post(
      'http://localhost:8080/api/v1/games/turn/regroup',
      dto,
      { responseType: 'text' }
    );
  }

  nextPhaseTurn(id: number) {
    return this.http.post(this.apiUrl+"/turns/"+ id +"/move-phase", null);
  }

  behaviorBot(gameId: number, playerGameBot: number, turnId: number) {
    return this.http.post<boolean>(`${this.apiUrl}/players/execute-turn-bot/${gameId}playerGameId=${playerGameBot}&turnId=${turnId}`, null);
  }

}
