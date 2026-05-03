import {Component, EventEmitter, inject, OnInit, Output} from '@angular/core';
import {Router} from '@angular/router';
import {Bot, Color, GameConfigForm} from '../../../core/models/interfaces/GameConfigModels';
import {AuthService} from '../../../core/services/auth.service';
import {FormsModule} from '@angular/forms';
import {CommonModule} from '@angular/common';

@Component({
  selector: 'app-game-options',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule
  ],
  templateUrl: './game-options.component.html',
  styleUrls: ['./game-options.component.css']
})
export class GameOptionsComponent implements OnInit{

  router: Router = inject(Router);

  authService: AuthService = inject(AuthService);

  @Output() changePageToPlayers = new EventEmitter<string>();
  @Output() submitForm = new EventEmitter<GameConfigForm>();

  options: GameConfigForm = {
    players: 3,
    bots: [],
    host: {
      name: null,
      color: {
        id: 1,
        name: ""
      }
    }
  }

  emitSubmit() {
    const colorId = Number(this.options.host.color.id);
    switch (colorId) {
      case 1:
        this.options.host.color.name = "Rojo ";
        break;
      case 2:
        this.options.host.color.name = "Azul";
        break;
      case 3:
        this.options.host.color.name = "Verde";
        break;
      case 4:
        this.options.host.color.name = "Amarillo";
        break;
      case 5:
        this.options.host.color.name = "Negro";
        break;
      case 6:
        this.options.host.color.name = "Magenta";
        break;
    }

    for (let i = 0; i < this.options.bots.length; i++) {
      if (this.options.bots.length > 0) {
        this.options.bots[i].color = this.setRandomColor();
      }
    }

    this.submitForm.emit(this.options);
  }

  setRandomColor(): Color {
    const colors: Color[] = []

    this.options.host.color.id = Number(this.options.host.color.id);
    colors.push(this.options.host.color);

    this.options.bots.forEach( bot => {
      colors.push(bot.color);
    })

    let id;
    let name = "";
    let newColor: Color = {
      id: 0,
      name: ""
    }
    do {
      id = Math.floor(Math.random() * 6) + 1;
      switch (id) {
        case 1: name = "Rojo"; break;
        case 2: name = "Azul"; break;
        case 3: name = "Verde"; break;
        case 4: name = "Amarillo"; break;
        case 5: name = "Negro"; break;
        case 6: name = "Magenta"; break;
      }
      newColor = { id, name };
    } while (colors.some(c => c.id === newColor.id));

    return newColor;
  }

  ngOnInit() {
    this.options.host.name = this.authService.getUserNameFromToken();
  }

  goLobby() {
    const confirmExit = window.confirm('¿Está seguro que desea volver al lobby? Se perderá tu configuracion.');
    if (confirmExit) {
      this.router.navigate(['/lobby']);
    }
  }

  /**
   * Maneja el click/change del checkbox del bot.
   * checkbox: HTMLInputElement desde template (#checkbox)
   * select: HTMLSelectElement desde template (#select)
   */
  checkBot(index: number, checkbox: HTMLInputElement, select: HTMLSelectElement) {
    const botId = index + 1;
    const bots: Bot[] = [...this.options.bots];

    const existingIndex = bots.findIndex(b => b.id === botId);

    if (checkbox && checkbox.checked) {
      // agregar o actualizar
      const difficulty = (select && select.value) ? select.value : 'Novice';
      if (existingIndex === -1) {
        const newBot: Bot = {
          id: botId,
          difficulty,
          color: { id: -1, name: 'notSet' },
          name: `Bot ${botId}`
        };
        bots.push(newBot);
      } else {
        bots[existingIndex].difficulty = difficulty;
      }
    } else {
      // quitar bot si existe
      if (existingIndex !== -1) {
        bots.splice(existingIndex, 1);
      }
    }

    this.options.bots = bots;
  }

  updateBotDifficulty(index: number, value: string) {
    const botId = index + 1;
    const bots = [...this.options.bots];
    const existing = bots.find(b => b.id === botId);
    if (existing) {
      existing.difficulty = value;
      this.options.bots = bots;
    }
  }

  addPlayers() {
    if (this.options.players < 6) {
      this.options.players++;
    }
  }

  removePlayers() {
    if (this.options.players > 3) {
      this.options.players--;
      // Trim bots to match new players count (players-1)
      const maxBots = this.options.players - 1;
      this.options.bots = this.options.bots.filter(b => b.id <= maxBots);
    }
  }

  protected readonly Array = Array;
}
