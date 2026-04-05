import {Component, ElementRef, EventEmitter, inject, OnInit, Output, QueryList, ViewChildren} from '@angular/core';
import {Router} from '@angular/router';
import {Bot, Color, GameConfigForm} from '../../../core/models/interfaces/GameConfigModels';
import {AuthService} from '../../../core/services/auth.service';
import {FormsModule} from '@angular/forms';

@Component({
  selector: 'app-game-options',
  imports: [
    FormsModule
  ],
  templateUrl: './game-options.component.html',
  styleUrl: './game-options.component.css'
})
export class GameOptionsComponent implements OnInit{

  router: Router = inject(Router);

  authService: AuthService = inject(AuthService);

  @Output() changePageToPlayers = new EventEmitter<string>();
  @Output() submitForm = new EventEmitter<GameConfigForm>();

  @ViewChildren('select') selectElements!: QueryList<ElementRef<HTMLSelectElement>>;
  @ViewChildren('checkbox') checkboxElements!: QueryList<ElementRef<HTMLInputElement>>;

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
        this.options.host.color.name = "Red";
        break;
      case 2:
        this.options.host.color.name = "Blue";
        break;
      case 3:
        this.options.host.color.name = "Green";
        break;
      case 4:
        this.options.host.color.name = "Yellow";
        break;
      case 5:
        this.options.host.color.name = "Black";
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
        case 1: name = "Red"; break;
        case 2: name = "Blue"; break;
        case 3: name = "Green"; break;
        case 4: name = "Yellow"; break;
        case 5: name = "Black"; break;
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

  checkBot(index: number) {
    const selectDifficulty = this.selectElements.get(index);
    const checkboxBot = this.checkboxElements.get(index);

    if (!selectDifficulty || !checkboxBot) {
      return;
    }

    let bots: Bot[] = this.options.bots;

    for (let i = 0; i < bots.length; i++) {
      if (bots.length > 0 && bots[i].name === "Bot " + (index+1)) {
        bots.splice(i, 1);
        this.options.bots = bots;
        return;
      }
    }

    let newBot: Bot = {
      id: index + 1,
      difficulty: selectDifficulty.nativeElement.value,
      color: {
        id: -1,
        name: "notSet"
      },
      name: "Bot " + (index + 1)
    }

    bots.push(newBot);
    this.options.bots = bots;
  }

  addPlayers() {
    this.options.players++;
  }

  removePlayers() {
    this.options.players--;
  }

  protected readonly Array = Array;
}

