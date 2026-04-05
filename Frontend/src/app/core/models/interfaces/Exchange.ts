import {PlayerGame} from './PlayerGame';
import {ExchangeCards} from './ExchangeCards';

export interface Exchange {
  id:number;
  playerGame: PlayerGame;
  quantityReceived: number;
  dateTime: Date;
  cardsExchange: ExchangeCards[];
}
