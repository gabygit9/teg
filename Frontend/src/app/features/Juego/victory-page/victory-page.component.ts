import {Component, computed, inject, Input, signal} from '@angular/core';
import {Router} from '@angular/router';

@Component({
  selector: 'app-victory-page',
  imports: [],
  templateUrl: './victory-page.component.html',
  styleUrl: './victory-page.component.css'
})
export class VictoryPageComponent {

  private router: Router = inject(Router);

  @Input() winner!: string;
  @Input() objectiveAchieved!: string;



  closeVictory() {
    this.router.navigate(['/lobby']);
  }
}
