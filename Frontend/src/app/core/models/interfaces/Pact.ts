import {Game} from './Game';
import {Player} from './Player';
import {PactType} from '../../enums/PactType';
import {Country} from '../../enums/Country';
import {PlayerGame} from './PlayerGame';

export interface Pact {
  id:number;
  game: Game;
  type: PactType;
  players: Player[];
  countries: Country[];
  active: boolean;
}
