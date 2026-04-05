import {Game} from './Game';
import {Player} from './Player';

export interface Message {
  id:number;
  gameId:number;
  senderId: number;
  content: string;
  isActive: boolean;
  isEdited: boolean;
  dateTime: string;
}
