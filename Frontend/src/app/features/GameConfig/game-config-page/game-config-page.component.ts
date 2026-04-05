import { Component } from '@angular/core';
import {GameOptionsComponent} from '../game-options/game-options.component';
import {GameConfigPlayersComponent} from '../game-config-players/game-config-players.component';
import {GameConfigForm} from '../../../core/models/interfaces/GameConfigModels';

@Component({
  selector: 'app-game-config-page',
  imports: [GameOptionsComponent, GameConfigPlayersComponent],
  templateUrl: './game-config-page.component.html',
  styleUrl: './game-config-page.component.css'
})
export class GameConfigPageComponent {

  currentPage = "game-options";

  gameOptions: GameConfigForm | any;

  changePage(page: string) {
    this.currentPage = page;
  }


  getForm(form: GameConfigForm) {
    this.gameOptions = form;
  }
}
