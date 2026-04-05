import {CountryGame} from './CountryGame';
import {LevelBot} from '../../enums/LevelBot';

export interface Player {
  id:number;
  name:string;
  availableArmies: number;
  territories: CountryGame[];
  levelBot?: LevelBot;
}
