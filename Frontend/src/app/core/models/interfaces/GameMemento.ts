import {Game} from './Game';

export interface GameMemento {
  mementoId: number;
  version: number;
  dateTime: Date;
  serializedState: string;
  game: Game;

}
