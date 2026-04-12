import {
  Component,
  computed,
  signal,
  inject,
  EventEmitter,
  Output,
  OnInit,
  Input,
  OnDestroy, ViewChild,
  OnChanges, SimpleChanges, numberAttribute
} from '@angular/core';
import {ObjectiveService} from '../../../core/services/objective.service';
import {CommonModule} from '@angular/common';
import {Router} from '@angular/router';
import {AuthService} from '../../../core/services/auth.service';
import {PlayerService} from '../../../core/services/player.service';
import {PlayerGame} from '../../../core/models/interfaces/PlayerGame';
import {PlayerCardSimple} from '../../../core/models/interfaces/PlayerCard';
import {CardService} from '../../../core/services/card.service';
import {CardsModalComponent} from '../../cards/cards-modal.component';
import {PlayerInfoComponent} from '../player/player-info/player-info.component';
import {GameService} from "../../../core/services/game.service";
import {CountryGameDTO} from "../../../core/models/interfaces/CountryGame";
import {AttackDto, AttackResultDto} from "../../../core/models/interfaces/AttackDto";
import {MementoService} from '../../../core/services/memento.service';
import {AttackResultComponent} from '../../attack-result/attack-result.component';
import {PlacementDTO} from '../../../core/models/interfaces/PlacementDTO';
import {Color} from '../../../core/enums/color';
import {GameDataDTO, PlayerGameDto} from '../../../core/models/interfaces/GameDataDTO';
import {Objective} from '../../../core/models/interfaces/Objective';
import {Player} from '../../../core/models/interfaces/Player';
import {Subscription} from 'rxjs';
import { ModalMessageService } from '../../../core/services/modal.service';
import {HistoryService} from '../../../core/services/history.service';


@Component({
  selector: 'app-actions-panel',
  imports: [
    CommonModule,
    CardsModalComponent,
    PlayerInfoComponent,
    AttackResultComponent,
  ],
  templateUrl: './actions-panel.component.html',
  styleUrl: './actions-panel.component.css'
})
export class ActionsPanelComponent implements OnInit, OnDestroy, OnChanges {
  @ViewChild(AttackResultComponent) modalAttack?: AttackResultComponent;
  @Input() gameId!: number;
  @Input() updateTurn?: boolean;
  @Input() selectedCountryToArmy?: CountryGameDTO;
  @Input() botEvent!: boolean;
  @Output() seeObjective = new EventEmitter<void>();
  @Output() playerInTurnUpdated = new EventEmitter<PlayerGame>();
  @Output() myPlayerUpdated = new EventEmitter<PlayerGame>();
  @Output() seeChat = new EventEmitter<void>();
  @Output() toggleHistoryEvent = new EventEmitter<void>();
  @Output() activeAttackMode = new EventEmitter<void>();
  @Output() deactivateAttackMode = new EventEmitter<void>();
  @Output() activeRegroupMode = new EventEmitter<void>();
  @Output() deactivateRegroupMode = new EventEmitter<void>();
  @Output() finishedTurn = new EventEmitter<void>();

  private subscriptions = new Subscription();
  router: Router = inject(Router);
  private authService = inject(AuthService);
  private intervalId: any;

  activeTurn = false;
  showTurn = true;
  private lastPlayerIdInTurn: number | null = null;
  availableActions: string[] = [];
  lastTurnId?: number;

  private intervalBotAction: any;
  private intervalVerifyTurn: any;

  playerInTurn?: PlayerGame;
  myPlayer: PlayerGame = {
    id: 0,
    color: Color.RED,
    objective: {
      id: 0,
      description: ""
    },
    player: {
      id: 0,
      name: "",
      availableArmies: 0,
      territories: [],
      levelBot: undefined
    },
    isHuman: true,
    deleted: false,
    isTurn: false,
    turnId: 0,
    currentPhase: "",
    armies: 0,
    countries: []
  };

  constructor(
    private objectiveService: ObjectiveService,
    private playerService: PlayerService,
    private cardService: CardService,
    private gameService: GameService,
    private mementoService: MementoService,
    private modalService: ModalMessageService,
    private eventService: HistoryService
  ) {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['gameId']) {
      console.log("Nuevo ID de partida recibido:", this.gameId);
      this.verifyTurn();
    }

    if (changes['botEvent'] && changes['botEvent'].currentValue === true) {
      this.startBehaviorBot();
    }

    if (changes['updateTurn'] && changes['updateTurn'].currentValue === true) {
      this.verifyTurn();
    }
  }

  ngOnInit(): void {
    console.log("ActionsPanelComponent cargado con partidaId:", this.gameId);
    if (!this.playerInTurn?.isHuman) {
      this.startBehaviorBot();
    }
    this.loadMyPlayer();
    this.intervalVerifyTurn = setInterval(() => {
      this.verifyTurn();
    }, 2000);
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
    clearInterval(this.intervalId);
    clearInterval(this.intervalBotAction);
    clearInterval(this.intervalVerifyTurn);
  }

  //#region referido al jugador
  loadMyPlayer() {
    const userId = this.authService.getUserIdFromToken();
    if (!userId) throw new Error("no se pudo conseguir el usuario de la partida");
    this.playerService.getBasePlayerByUserId(userId).subscribe({
      next: (jbase) => {
        this.myPlayer.player = jbase;
        const rawData = sessionStorage.getItem("gameData");
        if (rawData) {
          const gameData: GameDataDTO = JSON.parse(rawData);
          if (gameData.game) {
            const current = gameData.players.find(player =>
              numberAttribute(player.player.id) === this.myPlayer.player.id
            );
            if (current) {
              this.myPlayer = ActionsPanelComponent.mapToPlayerGame(current);
              this.myPlayerUpdated.emit(this.myPlayer);
            }
          }
        }
      },
      error: (err) => {
        console.error("Error al cargar myPlayer", err)
      }
    })
  }

  private verifyTurn() {
    this.playerService.getCurrentPlayerByGame(this.gameId).subscribe({
      next: (playerGame: PlayerGame | undefined) => {
        const playerTurnId : number | null = playerGame?.player.id ?? null;

        this.myPlayer?.id === playerGame?.id ? this.activeTurn = true : this.activeTurn = false;
        this.playerInTurn = playerGame;

        this.loadPlayerCountries();

        this.playerInTurnUpdated.emit(this.playerInTurn);

        if (this.activeTurn && playerGame?.turnId) {
          this.lastTurnId = playerGame?.turnId;
          this.gameService.getAvailableActions(playerGame.turnId).subscribe({
            next: (actions: string[]) => {
              this.availableActions = actions;
            },
            error: (err) => {
              console.error("Error al obtener acciones disponibles: ", err);
              this.availableActions = [];
            }
          });
        }

        if (this.lastPlayerIdInTurn !== playerTurnId) {
          this.showTurn = true;
          setTimeout(() => this.showTurn = false, 4000);
          this.lastPlayerIdInTurn = playerTurnId;
          this.checkLastEvents();
        }
      },
      error: (err) => {
        console.error("Error al obtener jugador en turno", err);
        this.activeTurn = false;
        this.playerInTurn = undefined;
        this.playerInTurnUpdated.emit(undefined);
        this.availableActions = [];
      }
    });
  }

  private loadPlayerCountries() {
    if (!this.playerInTurn?.id) {
      return;
    }
    this.gameService.getCountriesByPlayers(this.gameId, this.playerInTurn.player.id).subscribe({
      next: (countries) => {
        if (this.playerInTurn) {
          this.playerInTurn.countries = countries;
          this.playerInTurnUpdated.emit(this.playerInTurn);
        }
      },
      error: (err) => {
        console.error("Error cargando países:", err);
      }
    });
  }

  refreshPlayer() {
    const playerId = this.playerInTurn?.id;
    this.playerService.getPlayerInGame(this.gameId, playerId!).subscribe({
      next: (updatedPlayer: PlayerGame ) => {
        this.playerInTurn = {...updatedPlayer}
        console.log('Nuevo jugador: ', this.playerInTurn)
      },
      error: (err) => console.error("Error al refrescar jugador en turno", err)
    });
    this.playerService.getPlayerInGame(this.gameId, this.myPlayer.id).subscribe({
      next: (updatedPlayer: PlayerGame) => {
        this.myPlayer = {...updatedPlayer}
        this.myPlayerUpdated.emit(this.myPlayer);
      },
      error: (err) => console.error("Error al refrescar myPlayer", err)
    });
  }

  selectCountryToArmy(country: CountryGameDTO) {
    this.selectedCountryToArmy = country;
  }

  static mapToPlayerGame(playerGame: PlayerGameDto): PlayerGame {
    return {
      id: playerGame.id,
      color: playerGame.color as Color,
      objective: playerGame.objective as Objective,
      player: playerGame.player as unknown as Player,
      isHuman: playerGame.isHuman,
      deleted: playerGame.deleted,
      isTurn: playerGame.isTurn,
      turnId: 0,
      currentPhase: '',
      armies: 0,
      countries: undefined
    };
  }

  //#endregion

  //#region Getter Properties
  get canAttack(): boolean {
    return this.activeTurn && this.availableActions.includes("attack");
  }

  get canMove(): boolean {
    return this.activeTurn && this.availableActions.includes("moveArmies");
  }

  get canAskCards(): boolean {
    return this.activeTurn && this.availableActions.includes('askCard') && !this.askedCard;
  }

  get canExchange(): boolean {
    return this.activeTurn && this.availableActions.includes("exchange")
  }

  get canRequestArmies(): boolean {
    return this.activeTurn && this.availableActions.includes("requestArmies");
  }

  get canPutArmies(): boolean {
    return this.activeTurn && this.availableActions.includes(("putArmies"))
  }

  get playerCards(): PlayerCardSimple[] {
    return this._cards();
  }

  //#endregion

  //#region Atacar
  selectedCountryAttacker?: CountryGameDTO;
  selectedCountryDefensor?: CountryGameDTO;
  resultAttack?: AttackResultDto;
  attackMode = signal(false);
  openModalDice = signal(false);
  toastConquer = signal(false);
  hasConquer = signal(false);

  attack() {
    if (this.attackMode()) {
      this.deactivateAttackInternMode();
    } else {
      this.activeAttackInternMode();
    }
  }

  activeAttackInternMode() {
    if (!this.activeTurn || !this.availableActions.includes("attack")) {
      this.modalService.modalMessage('No es tu turno o no puedes atacar ahora.', 'Turnos');
      return;
    }
    this.attackMode.set(true);
    this.selectedCountryAttacker = undefined;
    this.selectedCountryDefensor = undefined;

    this.activeAttackMode.emit();
  }

  deactivateAttackInternMode() {
    this.attackMode.set(false);
    this.selectedCountryAttacker = undefined;
    this.selectedCountryDefensor = undefined;

    this.deactivateAttackMode.emit();
  }

  doAttack(dto: AttackDto) {
    this.gameService.doAttack(dto).subscribe({
      next: (res) => {
        console.log("Resultado del ataque:", res);
        this.resultAttack = res;
        this.openModalDice.set(true);
        this.modalAttack?.openModal(res);
        this.attackMode.set(false);
        this.refreshPlayer();

        if (res.wereConquered) {
          this.showConquerToast();
          if (!this.askedCard) {
            this.hasConquer.set(true)
          }
        }
      },
      error: (err) => {
        const message = err?.error?.message || err?.message || JSON.stringify(err);
        this.modalService.modalMessage('Error al atacar, algo salió mal.', 'Ataque');
        this.attackMode.set(false);
      }
    });
  }

  showConquerToast() {
    this.toastConquer.set(true);
    setTimeout(() => this.toastConquer.set(false), 3000);
  }

  //#endregion

  //#region Reagrupar
  modeRegroup = signal(false);

  move() {
    if (!this.activeTurn) return;
    this.modeRegroup.update(v => !v)
    if (this.modeRegroup()) {
      console.log('Botón Reagrupar clicked');
      this.activeRegroupMode.emit();
    } else {
      this.deactivateRegroupMode.emit();
    }

  }

  //#endregion

  //#region Pedir Tarjeta
  private _cards = signal<PlayerCardSimple[]>([]);
  showCardModal = signal(false);
  private askedCard: boolean = false;

  see_cards() {
    console.log("Se pidió ver las tarjetas");
    const playerGameId = this.myPlayer.id;
    if (!playerGameId) return;

    this.cardService.getPlayerCards(playerGameId).subscribe({
      next: (data) => {
        console.log("Datos crudos de tarjetas:", data);
        console.log("Primer tarjeta:", data[0]);

        this._cards.set(data);
        this.showCardModal.set(true);
      },
      error: (err) => console.error("Error al cargar tarjetas", err)
    });
  }

  askCard() {
    const playerId = this.myPlayer.id
    if (!playerId) return;

    this.subscriptions.add(
      this.cardService.askCard(playerId).subscribe({
        next: () => {
          this.refreshPlayer();
          this.hasConquer.set(false);
          this.askedCard = true;
          this.subscriptions.add(
            this.cardService.getPlayerCards(this.myPlayer.id).subscribe(cards => {
              if (cards.length === 6) {
                console.log(cards.length)
                this.see_cards();
              }
            })
          )
        },
        error: (err) => {
          console.error("No se pudo solicitar tarjeta:", err);
          this.modalService.modalMessage('No podés pedir tarjeta porque no conquistaste ningún país este turno.', 'Tarjetas');
        }
      })
    )

  }

  //#endregion

  //#region Colocar Ejércitos
  add_army(): void {
    if (!this.activeTurn || !this.availableActions.includes("putArmies")) {
      // alert("No podés incorporar ejércitos en esta fase.");
      this.modalService.modalMessage('No se pudo incorporar ejércitos en esta fase.', 'Añadir ejércitos');
      return;
    }

    if (!this.playerInTurn || !this.selectedCountryToArmy) {
      // alert("Seleccioná un país para colocar el ejército.");
      this.modalService.modalMessage('Seleccioná un país para colocar el ejército.', 'Ejércitos');
      return;
    }


    console.log('click en agregar ejército');
    const dto: PlacementDTO = {
      playerGameId: this.playerInTurn.id,
      countryId: this.selectedCountryToArmy.countryId,
      armies: 1
    };

    this.gameService.incorporateArmies(dto).subscribe({
      next: () => {
        console.log('Ejército colocado correctamente');
        this.refreshPlayer();
        this.loadPlayerCountries();
        this.verifyTurn();
      },
      error: (err) => {
        console.error('Error completo:', err);
        this.modalService.modalMessage('No se pudo colocar el ejército.', 'Añadir ejércitos');
      }
    });
  }

  //#endregion

  //#region Ver Objetivos
  objective = computed(() => this.objectiveService.objective());
  achieved = computed(() => this.objectiveService.achieved());
  showModal = signal(false);

  see_objectives() {
    this.objectiveService.objective.set(this.myPlayer.objective);
    this.showModal.set(true);
  }

  close_modal() {
    this.showModal.set(false);
  }

  //#endregion

  //#region Chat
  openChat(): void {
    this.seeChat.emit();
  }

  //#endregion

  //#region Historial
  toggleHistory(): void {
    this.toggleHistoryEvent.emit();
  }

  //#endregion

  //#region Guardar Partida
  saveGame(): void {
    if (!this.gameId) {
      // alert('No se encontró el ID de la partida.');
      this.modalService.modalMessage('No se encontró el ID de la partida.', 'Guardar partida');
      return;
    }
    this.mementoService.saveMemento(this.gameId).subscribe({
      next: (memento) => {
        console.log('Partida guardada:', memento);
        // alert('Partida guardada correctamente.');
        this.modalService.modalMessage('Partida guardada correctamente.', 'Guardar partida');
      },
      error: (err) => {
        console.error('Error al guardar la partida:', err);
        // alert('No se pudo guardar la partida.');
        this.modalService.modalMessage('No se pudo guardar la partida.', 'Guardar partida');
      }
    });
  }

  //#endregion

  //#region Finalizar Turno
  lastTurnDto = {
    turnId: 0,
    playerGameId: 0
  }

  finish(): void {
    if (!this.playerInTurn) return;

    const endTurnDto = {
      turnId: this.playerInTurn.turnId,
      playerGameId: this.playerInTurn.id
    };

    if (this.lastTurnDto === endTurnDto) {
      return;
    }
    this.lastTurnDto = endTurnDto;

    if (this.playerInTurn.turnId) {

    }

    this.verifyTurn();
    this.askedCard = false;
    this.hasConquer.set(false);
    this.finishedTurn.emit();
  }

  //#endregion

  //#region Pasar Fase
  movePhaseCooldown: boolean = false;

  movePhase() {
    this.intervalId = setTimeout(() => {
      this.movePhaseCooldown = false
    }, 2000);
    this.subscriptions.add(
      this.gameService.nextPhaseTurn(this.playerInTurn!.turnId).subscribe({
        next: () => {
          console.log("Fase avanzada en turno");
        },
        error: (err) => {
          console.error("Error al avanzar de fase en el turno.", err);
        }
      })
    )
  }

  //#endregion

  //#region Menu
  showMenuModal = signal(false);

  menu() {
    this.showMenuModal.set(true);
  }

  closeMenuModal() {
    this.showMenuModal.set(false);
  }

  returnLobby() {
    const confirmExit = window.confirm('¿Está seguro que desea volver al lobby? Se perderá el progreso actual.');
    if (confirmExit) {
      this.router.navigate(['/lobby']);
    }
  }

  returnLogin() {
    const confirmExit = window.confirm('¿Está seguro que desea volver al login? Se perderá el progreso actual.');
    if (confirmExit) {
      this.authService.logout();
      this.router.navigate(['/login']);
    }
  }

  //#endregion

  //region comportamiento bot

  private botVerifyAction: number = -1;

  private startBehaviorBot(): void {
    // Limpiar intervalo anterior si existe
    if (this.intervalBotAction) {
      clearInterval(this.intervalBotAction);
    }

    const randomDelay = (Math.floor(Math.random() * (9 - 5 + 1)) + 5) * 1000;
    this.intervalBotAction = setInterval(() => {
      this.behaviorBot();
    }, randomDelay);
  }

  behaviorBot() {
    if (this.playerInTurn?.isHuman === true) {
      return;
    }

    if (this.playerInTurn?.turnId === this.lastTurnId) {
      this.finish();
      return;
    }

    const botId = this.playerInTurn?.id;
    const turnId = this.playerInTurn?.turnId;

    if (!botId || !turnId) return;

    if (this.botVerifyAction === botId) {
      console.log("Acción evitada");
      this.finish();
      return;
    }

    this.botVerifyAction = botId;

    console.log("COMPORTAMIENTO BOT: el bot " + this.playerInTurn!.player.name + " realizara su acción");

    this.subscriptions.add(
      this.gameService.behaviorBot(this.gameId, botId, turnId).subscribe({
        next: () => {
          this.finish();
        },
        error: (err) => {
          console.error("Error al ejecutar el comportamiento del bot", err);
        }
      })
    );
  }

  //endregion

  attackMessage: string = '';
  conquerMessage: string = '';

  checkLastEvents() {
    this.eventService.getHistory(this.gameId).subscribe({
      next: (events) => {
        if (!events || events.length === 0) {
          return;
        }

        const attackEvents = events.filter(e => e.description.includes('attacked'));
        const conquerEvents = events.filter(e => e.description.includes('conquered'));

        const lastAttack = attackEvents.length > 0 ? attackEvents[attackEvents.length - 1] : null;
        const lastConquer = conquerEvents.length > 0 ? conquerEvents[conquerEvents.length - 1] : null;

        if (lastAttack && this.attackMessage !== lastAttack.description) {
          this.attackMessage = lastAttack.description;
          setTimeout(() => {
            this.attackMessage = '';
          }, 5000);
        }

        if (lastConquer && this.conquerMessage !== lastConquer.description) {
          this.conquerMessage = lastConquer.description;
          setTimeout(() => {
            this.conquerMessage = '';
          }, 5000);
        }
      },
      error: () => {}
    });
  }

}
