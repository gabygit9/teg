import { CountryGameDTO } from './CountryGame';

export interface GameDataDTO {
  game: GameResponseDTO;
  players: PlayerGameDto[];
  countries: CountryGameDTO[];
}

export interface GameResponseDTO {
  gameId: number;
  commonObjective: string;
  communicationType: string;
  dateTime: string;
}

export interface PlayerGameDto {
  id: number;
  color: string;
  objective: ObjectiveDto;
  player: BasePlayerDTO;
  isHuman: boolean;
  deleted: boolean;
  isTurn: boolean;
}

export interface ObjectiveDto {
  id: number;
  description: string;
}

export interface BasePlayerDTO {
  id: number;
  playerName: string;
  availableArmies: number;
}
