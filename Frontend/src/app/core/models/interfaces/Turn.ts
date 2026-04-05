import {PlayerGame} from './PlayerGame';
import {Game} from './Game';
import {TurnPhase} from '../../enums/TurnPhase';

export interface Turn {
  id: number;
  playerGame: PlayerGame;
  startDate: string;
  currentPhase: TurnPhase;
  armiesAvailable: number;
}
