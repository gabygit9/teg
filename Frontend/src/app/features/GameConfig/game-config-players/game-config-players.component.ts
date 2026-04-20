import {Component, EventEmitter, inject, Input, OnDestroy, Output} from '@angular/core';
import {GameConfigForm} from '../../../core/models/interfaces/GameConfigModels';
import {GameService} from '../../../core/services/game.service';
import {Observable, of, Subscription, switchMap} from 'rxjs';
import {GameDataDTO} from "../../../core/models/interfaces/GameDataDTO";
import {Router} from "@angular/router";
import {DifficultyPipe} from '../../../shared/pipes/difficulty.pipe';

@Component({
    selector: 'app-game-config-players',
    imports: [DifficultyPipe],
    templateUrl: './game-config-players.component.html',
    styleUrl: './game-config-players.component.css'
})
export class GameConfigPlayersComponent implements OnDestroy {

    gameService: GameService = inject(GameService);
    router: Router = inject(Router);

    @Input() options: GameConfigForm | any;

    @Output() changePageToOptions = new EventEmitter<string>();

    private currentGameId: number = 0;

    subscriptions: Subscription = new Subscription();

    disableButtons = false;

    ngOnDestroy() {
        this.subscriptions.unsubscribe();
    }

    startGame() {
      this.disableButtons = true;
        const newGame = {
            communicationType: {
                id: 1,
                description: null
            },
            commonObjectiveId: 16
        };
        this.subscriptions.add(
            this.gameService.gameStart(newGame).pipe(
                switchMap(gameResponse => {
                    this.currentGameId = gameResponse.responseId;
                    return this.gameService.assignHumanInGame(this.currentGameId, this.options.host.name, this.options.host.color.id);
                }),
                switchMap(() => {
                    return this.assignBotsSequentially(0);
                }),
                switchMap(() => {
                    return this.gameService.gameNextState(this.currentGameId);
                }),
                switchMap(() => {
                    return this.gameService.getGameData(this.currentGameId);
                }),
            ).subscribe({
                next: (res: GameDataDTO) => {
                    console.log("datos de la partida: ", res);
                    sessionStorage.setItem("gameData", JSON.stringify(res));
                    this.router.navigate(['/game']);
                },
                error: (error) => {
                    console.error("Error en la creación de partida: ", error);
                }
            })
        );
    }

    private assignBotsSequentially(index: number): Observable<void> {
        if (index >= this.options.bots.length) {
            return of(undefined); // Fin de bots
        }

        const bot = this.options.bots[index];
        let difficultyId = 0;
        switch (bot.difficulty.toLowerCase()) {
            case "novice":
                difficultyId = 1;
                break;
            case "balanced":
                difficultyId = 2;
                break;
            case "expert":
                difficultyId = 3;
                break;
        }

        return this.gameService.assignBotInGame(this.currentGameId, difficultyId, bot.color.id).pipe(
            switchMap(() => this.assignBotsSequentially(index + 1)) // Llama al siguiente bot
        );
    }
}
