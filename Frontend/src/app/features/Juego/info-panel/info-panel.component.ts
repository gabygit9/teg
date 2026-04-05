import {
  Component,
  EventEmitter,
  inject,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  Output,
  SimpleChanges
} from '@angular/core';
import {Subscription} from 'rxjs';
import {GameService} from '../../../core/services/game.service';
import {PlayerGame} from '../../../core/models/interfaces/PlayerGame';
import {EndTurnDTO} from '../../../core/models/interfaces/EndTurnDTO';


@Component({
  selector: 'app-info-panel',
  templateUrl: './info-panel.component.html',
  styleUrl: './info-panel.component.css'
})
export class InfoPanelComponent implements OnInit, OnDestroy, OnChanges {

  @Input() playerInTurn?: PlayerGame;
  @Input() resetTimerSignal?: boolean;
  @Output() refreshTurn = new EventEmitter<boolean>();
  @Output() executeBot = new EventEmitter<boolean>();


  players: {
    name: string;
    colorCSS: string
  }[] = [];

  time: number = 0;
  isPaused: boolean = false;
  turnFinished: boolean = false;
  private interval: any;

  timeSeconds: number = 0;
  timeMinutes: number = 0;
  private gameInterval: any;

  ngOnInit() {
    this.startTurnCounter();
    this.startGameTimer();
    this.loadPlayers();
  }


  gameService: GameService = inject(GameService);
  subscriptions: Subscription = new Subscription();

  // Cronometro de turno
  startTurnCounter() {
    this.turnFinished = false;
    this.time = 180; // puede ser que para jugar sea 180

    this.interval = setInterval(() => {
      if (!this.isPaused) {
        this.time--;

        if (this.time <= 0) {
          clearInterval(this.interval);
          this.turnFinished = true;
          this.endTurn();

          setTimeout(() => {
            this.turnFinished = false;
            this.startTurnCounter();
          }, 3000);
        }
      }
    }, 1000);

  }

  endTurn() {
    if (this.playerInTurn === undefined) {
      console.error("El jugador en turno no está definido.")
      return;
    }
    const playerOnTurn: EndTurnDTO = {
      turnId: this.playerInTurn.turnId,
      playerGameId: this.playerInTurn.id
    }

    this.subscriptions.add(
      this.gameService.postEndTurn(playerOnTurn).subscribe({
        next: () => {
          this.refreshTurn.emit(true);
          setTimeout(() => this.refreshTurn.emit(false), 1000);
          if (!this.playerInTurn?.isHuman) {
            this.executeBot.emit(true);
            setTimeout(() => this.executeBot.emit(false), 1000);
          }
        },
        error: (error) => {
          console.error("Error al finalizar el turno", error);
        }
      })
    );
  }

  // Cronometro global
  startGameTimer() {
    this.timeSeconds = 0;
    this.timeMinutes = 0;

    this.gameInterval = setInterval(() => {
      if (!this.isPaused) {
        this.timeSeconds++;

        if (this.timeSeconds >= 60) {
          this.timeSeconds = 0;
          this.timeMinutes++;
        }
      }
    }, 1000);
  }

  private mapColor(colorEsp: string): string {
    const map: Record<string, string> = {
      red: 'red',
      blue: 'blue',
      green: 'green',
      yellow: 'yellow',
      black: 'black',
      magenta: 'magenta'
    };
    return map[colorEsp.toLowerCase()] || 'gray';
  }

  loadPlayers(): void {
    const data = sessionStorage.getItem("gameData");
    if (!data) return;

    try {
      const parsed = JSON.parse(data);
      this.players = parsed.players.map((j: any) => ({
        name: j.player.name,
        colorCSS: this.mapColor(j.color)
      }));
    } catch (e) {
      console.error("Error al cargar jugadores desde gameData (en el header)", e);
    }
  }

  togglePause() {
    this.isPaused = !this.isPaused;
  }

  ngOnDestroy(): void {
    clearInterval(this.interval);
    this.subscriptions.unsubscribe();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['resetTimerSignal'] && changes['resetTimerSignal'].currentValue === true) {
      this.time = 1;
    }
  }
}
