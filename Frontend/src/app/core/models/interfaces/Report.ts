import {Game} from './Game';

export interface Report {
  id:number;
  game: Game;
  accuser:string; //acusador
  accuse: string;  //acusado
  motive: string;
  dateTime: string;
  resolved: boolean;
}
