import {Component, inject, numberAttribute, OnDestroy, OnInit, signal, ViewChild} from '@angular/core';
import {PlayerComponent} from '../player/player.component';
import {CommonModule} from '@angular/common';
import {ActionsPanelComponent} from '../actions-panel/actions-panel.component';
import {MapComponent} from '../map/map.component';
import {InfoPanelComponent} from '../info-panel/info-panel.component';
import {HistoryComponent} from '../history/history.component';
import {PlayerGame} from '../../../core/models/interfaces/PlayerGame';
import {ChatComponent} from '../../lobby/chat/chat.component';
import {GameDataDTO, CountryGameDTO, PlayerGameDto} from '../../../core/models/interfaces/GameDataDTO';
import {AttackDto} from "../../../core/models/interfaces/AttackDto";
import {GameService} from '../../../core/services/game.service';
import {CountryGameDTO} from '../../../core/models/interfaces/CountryGame';
import {ArmyRegroupDTO} from '../../../core/models/interfaces/ArmyRegroupDTO';
import {Subscription} from 'rxjs';
import {VictoryPageComponent} from '../victory-page/victory-page.component';
import {ModalMessageService} from '../../../core/services/modal.service';
import {RightPanelComponent} from '../right-panel/right-panel.component';


@Component({
  selector: 'app-game-page',
  imports: [
    PlayerComponent,
    CommonModule,
    ActionsPanelComponent,
    MapComponent,
    InfoPanelComponent,
    HistoryComponent,
    ChatComponent,
    VictoryPageComponent,
    RightPanelComponent
  ],
  templateUrl: './game-page.component.html',
  styleUrl: './game-page.component.css'
})
export class GamePageComponent implements OnInit, OnDestroy {
  @ViewChild(ActionsPanelComponent)
  actionsPanel!: ActionsPanelComponent;

  @ViewChild('historyComp')
  historyComp?: HistoryComponent;

  private subscriptions: Subscription = new Subscription();

  countries: CountryGameDTO[] = [];

  private isLoadedGame = false;
  private restoredState: any = null;
  private intervalId: any;
  attackMode = false;

  playerId!: number; //quitar?

  playerInTurn?: PlayerGame;
  myPlayer?: PlayerGame;
  seeObjective: boolean = false;
  openChat: boolean = false;
  openHistory: boolean = false;
  historyIntervalId: any;
  gameId = 0;
  countrySelectedToArmy?: CountryGameDTO;
  countryOriginRegroup?: CountryGameDTO;
  countryDestinationRegroup?: CountryGameDTO;
  regroupMode: boolean = false;
  resetTimer: boolean = false;
  refreshTurn?: boolean;
  botEvent: boolean = false;

  showVictory = signal(false);
  winner?: PlayerGameDto;


  onCountrySelectedToArmy(country: CountryGameDTO) {
    this.countrySelectedToArmy = country;
    console.log('País seleccionado:', country);
  }

  ngOnInit() {
    this.verifyRestoredState();

    if (!this.isLoadedGame) {
      this.refreshData();
      this.intervalId = setInterval(() => this.refreshData(), 1000);
    }
  }

  constructor( private gameService: GameService,
               private modalService: ModalMessageService
  ) {
    const rawData = sessionStorage.getItem("gameData");
    if (rawData) {
      const gameData: GameDataDTO = JSON.parse(rawData);
      if (gameData.game) {
        this.gameId = gameData.game.gameId;
        /*this.winner = gameData.players[0];
        this.showVictory = signal(true);*/
      }
    }
  }


  private verifyRestoredState(): void {
    const stateRestoredStr = sessionStorage.getItem('restoredState');
    const gameIdStr = sessionStorage.getItem('selectedGameId');

    if (stateRestoredStr && gameIdStr) {
      try {
        this.restoredState = JSON.parse(stateRestoredStr);
        this.gameId = parseInt(gameIdStr);
        this.isLoadedGame = true;

        console.log('Estado restaurado detectado:', this.restoredState);
        console.log('Partida ID:', this.gameId);

        this.loadRestoredState();

        sessionStorage.removeItem('restoredState');
        sessionStorage.removeItem('selectedGameId');

        setTimeout(() => {
          // alert(`Partida ${this.partidaId} cargada exitosamente!`);
          this.modalService.modalMessage(`Partida ${this.gameId} cargada exitosamente!`, 'Partida');

        }, 500);

      } catch (error) {
        console.error('Error al parsear el estado restaurado:', error);
        this.isLoadedGame = false;
      }
    }
  }

  private loadRestoredState(): void {
    if (!this.restoredState) return;

    try {
      if (this.restoredState.countries) {
        this.countries = this.restoredState.countries;
        sessionStorage.setItem('gameData', JSON.stringify(this.restoredState));
        console.log('Países cargados desde estado restaurado:', this.countries);
      }

      if (this.restoredState.players) {
        console.log('Jugadores del estado restaurado:', this.restoredState.players);
      }

      if (this.restoredState.playerInTurn) {
        this.playerInTurn = this.restoredState.playerInTurn;
        console.log('Jugador en turno restaurado:', this.playerInTurn);
      }

      if (this.restoredState.playerId) {
        this.playerId = this.restoredState.playerId;
        console.log('Jugador ID restaurado:', this.playerId);
      }

      setTimeout(() => {
        this.intervalId = setInterval(() => this.refreshData(), 1000);
      }, 2000);

    } catch (error) {
      console.error('Error al cargar el estado restaurado:', error);
      // alert('Error al cargar la partida. Se iniciará el juego normalmente.');
      this.modalService.modalMessage('Error al cargar la partida. Se iniciará el juego normalmente.', 'Partida');

      this.isLoadedGame = false;
      this.refreshData();
      this.intervalId = setInterval(() => this.refreshData(), 5000);
    }
  }

  refreshData() {
    this.gameService.getGameData(this.gameId).subscribe({
      next: (data: GameDataDTO) => {
        sessionStorage.setItem('gameData', JSON.stringify(data));
        this.countries = data.countries;

        if (data.game) {
          const current = data.players.find(player =>
            numberAttribute(player.player.id) === this.myPlayer?.player.id
          );
          if (current) {
            this.myPlayer = ActionsPanelComponent.mapToPlayerGame(current);
          }
        }

        if (this.isLoadedGame && this.restoredState) {
          this.mergeWithRestoredState(data);
          this.isLoadedGame = false;
          this.restoredState = null;
        }
      },
      error: (err) => {
        console.error('Error al refrescar datos:', err);
      }
    });
  }

  private mergeWithRestoredState(currentData: GameDataDTO): void {
    console.log('Haciendo merge entre datos actuales y restaurados');
    console.log('Datos actuales:', currentData);
    console.log('Datos restaurados:', this.restoredState);
  }

  onActiveAttackMode() {
    if (this.playerInTurn) {
      this.playerInTurn.player.territories?.forEach(country => {
        console.log(`País ${country.country}: ${country.armiesAmount} ejércitos`);
      });
    }
    this.attackMode = true;
  }

  onDeactivateAttackMode() {
    this.attackMode = false;
  }

  resolveAttack(dto: AttackDto) {
    console.log(`País atacante tiene ${dto.dice + 1} ejércitos (dados: ${dto.dice})`);
    this.actionsPanel.doAttack(dto);
    this.onDeactivateAttackMode();
  }

  updatePlayerInTurn(player: PlayerGame) {
    this.playerInTurn = {...player};
    this.refreshData();
  }

  updateMyPlayer(player: PlayerGame) {
    this.myPlayer = {...player};
    this.refreshData();
  }

  onToggleObjective() {
    this.seeObjective = !this.seeObjective;
  }

  onToggleChat() {
    this.openChat = !this.openChat;
  }

  onToggleHistory(): void {
    this.openHistory = !this.openHistory;

    if (this.openHistory) {
      setTimeout(() => {
        this.historyComp?.reloadHistory();

        this.historyIntervalId = setInterval(() => {
          this.historyComp?.reloadHistory();
        }, 1000);
      }, 0);
    } else {
      clearInterval(this.historyIntervalId);
    }
  }

  ngOnDestroy() {
    this.subscriptions.unsubscribe();
    if (this.historyIntervalId) {
      clearInterval(this.historyIntervalId);
    }
    if (this.intervalId) {
      clearInterval(this.intervalId);
    }
  }

  onCountryRegroupSelectedDestine(country: CountryGameDTO) {
    this.countryDestinationRegroup = country;
    this.regroupArmies(this.countryOriginRegroup, country);
  }

  onCountryRegroupSelectedOrigin(country: CountryGameDTO) {
    this.countryOriginRegroup = country;
  }

  activeModeRegroup() {
    console.log('Modo reagrupe ACTIVADO');
    this.regroupMode = true;
    this.countryOriginRegroup = undefined;
    this.countryDestinationRegroup = undefined;
  }

  deactivateModeRegroup() {
    this.regroupMode = false;
    this.countryOriginRegroup = undefined;
    this.countryDestinationRegroup = undefined;
  }

  regroupArmies(origin?: CountryGameDTO, destine?: CountryGameDTO): void {
    if (!origin || !destine) return;

    const dto: ArmyRegroupDTO = {
      playerId: this.playerInTurn?.id ?? 0,
      originId: origin.countryId,
      destineId: destine.countryId,
      amount: 1
    };

    this.gameService.armyRegroup(dto).subscribe({
      next: (res) => {
        console.log('Ejército reagrupado:', res);
        this.refreshData(); //
      },
      error: (err) => {
        console.error('Error:', err);
        // alert('No se pudo reagrupar.');
        this.modalService.modalMessage('No se pudo reagrupar.', 'Ejército reagrupado');
      },
      complete: () => {
        console.log('Movimiento completado');
      }
    });
  }

  onFinishedTurn() {
    this.resetTimer = true;
    setTimeout(() => this.resetTimer = false, 500);
  }
}

