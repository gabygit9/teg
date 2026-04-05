import {Country} from '../../enums/Country';
import {Game} from './Game';

export interface CountryGame {
  country: Country;
  game: Game;
  player: number;
  armiesAmount: number;
}

export interface CountryGameDTO {
  countryId: number;
  gameId: number;
  countryName: string;
  continent: string;
  amountArmies: number;

  playerId: number;
  playerName: string;
  color: string;
}
