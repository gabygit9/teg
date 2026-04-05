import {Component, Input, OnDestroy, OnInit} from '@angular/core';
import {PlayerGame} from '../../../core/models/interfaces/PlayerGame';
import {PlayerService} from '../../../core/services/player.service';
import {CommonModule} from '@angular/common';
import {AuthService} from '../../../core/services/auth.service';
import {PlayerGameDto} from '../../../core/models/interfaces/GameDataDTO';

@Component({
  selector: 'app-player',
  imports: [
    CommonModule
  ],
  templateUrl: './player.component.html',
  styleUrl: './player.component.css'
})
export class PlayerComponent {
  @Input() seeObjective = false;
  @Input() gameId!: number;
  @Input() playerInTurn?: PlayerGame;


  constructor(
    private playerService: PlayerService,
    private authService: AuthService
  ) {}


  /*get isMyTurn(): boolean{
    const myId = this.authService.getUserIdFromToken();
    const isTurn = this.playerInTurn?.player.id === miId && this.playerInTurn?.isTurn;
    console.log(`Mi ID: ${myId}, ID player: ${this.playerGame?.player.id}, isTurn: ${this.playerGame?.isTurn}, esMiTurno: ${isTurn}`);
    return isTurn;
  }
   */

  getColorHex(color?: string): string {
    const colorMap: Record<string, string> = {
      red: 'red',
      blue: 'blue',
      green: 'green',
      yellow: 'yellow',
      black: 'black',
      magenta: 'magenta'
    };

    return color ? colorMap[color.toLowerCase()] || 'gray' : 'gray';
  }


}
//MUESTRA INFO DEL JUGADOR ACTUAL(TROPAS, TARJETAS, OBJETIVO)
