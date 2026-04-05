import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';
import { ModalMessageService } from '../../core/services/modal.service';

interface PlayerStatistics {
  id: number;
  name: string;
  color: string;
  position: number;
  countriesConquered: number;
  armiesUsed: number;
  battlesWon: number;
  lostBattles: number;
  timePlayed: string;
  winner: boolean;
  isCurrentUser: boolean;
  objectiveAchieved: string;
}

interface GameResult {
  id: number;
  name: string;
  startDate: string;
  finishDate: string;
  totalDuration: string;
  winner: PlayerStatistics;
  players: PlayerStatistics[];
  generalStatistics: {
    totalBattles: number;
    mostDisputedCountry: string;
    greatestConquest: string;
    quickGame: boolean;
  };
}

@Component({
  selector: 'app-victoria-page',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './victory-page.component.html',
  styleUrl: './victory-page.component.css'
})
export class VictoryPageComponent implements OnInit {

  result: GameResult | null = null;
  loading = true;
  showConfetti = true;
  gameId: number | null = null;

  // Animaciones
  showStatistics = false;
  showRanking = false;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private authService: AuthService,
    private modalService: ModalMessageService
  ) {}
  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.gameId = params['gameId'] ? parseInt(params['gameId']) : null;
      this.loadResults();
    });

    setTimeout(() => this.showStatistics = true, 2000);
    setTimeout(() => this.showRanking = true, 3000);
    setTimeout(() => this.showConfetti = false, 8000);
  }

  async loadResults(): Promise<void> {
    try {
      this.loading = true;

      // Simular carga de datos
      await this.loadSimulatedResults();

      // TODO - Implementar esto
      // Cuando esté la API real, usar esto:
      // const result = await this.gameService.getGameResults(this.gameId).toPromise();
      // this.result = this.processResults(result);

    } catch (error) {
      console.error('Error cargando resultados:', error);
      this.router.navigate(['/lobby']);
    } finally {
      this.loading = false;
    }
  }

  private async loadSimulatedResults(): Promise<void> {
    await new Promise(resolve => setTimeout(resolve, 1500));

    const userName = this.authService.getUserNameFromToken() || 'TuNombre';

    this.result = {
      id: this.gameId || 1,
      name: 'Conquista Mundial Épica',
      startDate: '2025-01-20T14:30:00Z',
      finishDate: '2025-01-20T16:45:00Z',
      totalDuration: '2h 15m',
      winner: {
        id: 1,
        name: userName,
        color: 'blue',
        position: 1,
        countriesConquered: 28,
        armiesUsed: 245,
        battlesWon: 18,
        lostBattles: 3,
        timePlayed: '2h 15m',
        winner: true,
        isCurrentUser: true,
        objectiveAchieved: 'Conquistar 24 países'
      },
      players: [
        {
          id: 1,
          name: userName,
          color: 'blue',
          position: 1,
          countriesConquered: 28,
          armiesUsed: 245,
          battlesWon: 18,
          lostBattles: 3,
          timePlayed: '2h 15m',
          winner: true,
          isCurrentUser: true,
          objectiveAchieved: 'Conquistar 24 países'
        },
        {
          id: 2,
          name: 'Estratega_Pro',
          color: 'red',
          position: 2,
          countriesConquered: 15,
          armiesUsed: 198,
          battlesWon: 12,
          lostBattles: 8,
          timePlayed: '2h 10m',
          winner: false,
          isCurrentUser: false,
          objectiveAchieved: 'Eliminar jugador específico'
        },
        {
          id: 3,
          name: 'Bot_Experto',
          color: 'green',
          position: 3,
          countriesConquered: 7,
          armiesUsed: 167,
          battlesWon: 8,
          lostBattles: 12,
          timePlayed: '1h 45m',
          winner: false,
          isCurrentUser: false,
          objectiveAchieved: 'Conquistar América del Sur'
        }
      ],
      generalStatistics: {
        totalBattles: 38,
        mostDisputedCountry: 'Egipto',
        greatestConquest: 'América del Norte en 15 minutos',
        quickGame: false
      }
    };
  }

  getMedal(position: number): string {
    switch (position) {
      case 1: return '🥇';
      case 2: return '🥈';
      case 3: return '🥉';
      default: return '🏅';
    }
  }

  getColorHex(color: string): string {
    const colors: Record<string, string> = {
      red: '#ef4444',
      blue: '#3b82f6',
      green: '#10b981',
      yellow: '#f59e0b',
      purple: '#8b5cf6',
      orange: '#f97316'
    };
    return colors[color] || '#6b7280';
  }

  getPercentageWin(player: PlayerStatistics): number {
    const total = player.battlesWon + player.lostBattles;
    return total > 0 ? Math.round((player.battlesWon / total) * 100) : 0;
  }

  formatDuration(dateParam: string): string {
    const date = new Date(dateParam);
    return date.toLocaleTimeString('es-ES', {
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  newGame(): void {
    this.router.navigate(['/game-config']);
  }

  returnLobby(): void {
    this.router.navigate(['/lobby']);
  }

  seeCompleteStatistics(): void {
    // Por ahora solo mostrar alert, después se puede hacer una página dedicada
    // alert('Función de estadísticas completas próximamente...');
    this.modalService.modalMessage(`Función de estadísticas completas próximamente...`, 'Estadisticas');

  }

  shareResult(): void {
    if (navigator.share) {
      navigator.share({
        title: 'Victoria en TEG',
        text: `¡Acabo de ganar una partida de TEG conquistando ${this.result?.winner.countriesConquered} países!`,
        url: window.location.href
      }).catch(console.error);
    } else {
      const text = `¡Acabo de ganar una partida de TEG conquistando ${this.result?.winner.countriesConquered} países!`;
      navigator.clipboard.writeText(text).then(() => {
        // alert('¡Resultado copiado al portapapeles!');
        this.modalService.modalMessage(`¡Resultado copiado al portapapeles!`, 'Compartir Resultado');
      }).catch(() => {
        // alert('No se pudo compartir el resultado');
        this.modalService.modalMessage(`No se pudo compartir el resultado`, 'Compartir Resultado');
      });
    }
  }

  trackByPlayer(index: number, player: PlayerStatistics): number {
    return player.id;
  }
}

