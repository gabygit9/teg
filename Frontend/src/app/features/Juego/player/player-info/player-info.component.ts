import {ChangeDetectorRef, Component, Input, OnChanges, SimpleChanges} from '@angular/core';
import {PlayerGame} from '../../../../core/models/interfaces/PlayerGame';
import {PlayerService} from '../../../../core/services/player.service';
import {CommonModule} from '@angular/common';

@Component({
  selector: 'app-player-info',
  imports: [CommonModule],
  templateUrl: './player-info.component.html',
  styleUrl: './player-info.component.css'
})
export class PlayerInfoComponent implements OnChanges{
  @Input() player?: PlayerGame;

  availableArmies: number = 0;

  constructor(
    private playerService: PlayerService,
    private cd: ChangeDetectorRef
    ) {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['player'] && this.player?.player?.availableArmies != null) {
      this.availableArmies = this.player.player.availableArmies;
      this.cd.detectChanges();
      //console.log("Cambios detectados en jugador:", changes['jugador']);
    }
  }
}
