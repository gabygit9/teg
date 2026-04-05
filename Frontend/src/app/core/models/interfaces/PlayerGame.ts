import { Objective } from "./Objective";
import { Color } from "../../enums/color";
import {Player} from './Player';
import {CountryGameDTO} from './CountryGame';

export interface PlayerGame {
  id: number;
  color: Color;
  objective: Objective;
  player: Player;
  isHuman: boolean;
  deleted: boolean;
  isTurn: boolean;
  turnId: number;
  currentPhase: string;
  armies: number;
  countries?: CountryGameDTO[];
}
