import {PlayerGame} from './PlayerGame';
import {CountryCard} from './CountryCard';

export interface PlayerCard {
  id:number;
  playerGame: PlayerGame;
  countryCard: CountryCard;
  used: boolean;
}

export interface PlayerCardSimple {
  id:number;
  country: string;
  symbol: string;
  used: boolean;
}
