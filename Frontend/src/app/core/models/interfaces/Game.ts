import { PlayerGame } from "./PlayerGame";
import { Objective } from "./Objective";
import { GameStatus } from "../../enums/GameStatus";
import { CommunicationType } from "../../enums/CommunicationType";

export interface Game {
  id: number;
  creationDate: string;
  gameStatus: GameStatus;
  communicationType: CommunicationType;
  commonObjective: Objective;
  players: PlayerGame[];
}

export interface GameDTO {
  responseId: number;
  commonObjectiveId: number;
  objective: {
    id: number;
    description: string;
  };
  stateId: number;
  state: {
    id: number;
    description: string;
  };
  communicationType: {
    id: number;
    description: string;
  };
  dateTime: string;
}
