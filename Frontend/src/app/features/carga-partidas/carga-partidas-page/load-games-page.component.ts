import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { GameService } from '../../../core/services/game.service';
import { AuthService } from '../../../core/services/auth.service';
import { Game } from '../../../core/models/interfaces/Game';
import { GameStatus } from '../../../core/enums/GameStatus';

interface SavedGame {
  id: number;
  name: string;
  creationDate: string;
  lastPlayDate: string;
  players: string[];
  progress: number; // porcentaje de progreso
  state: GameStatus;
  imagen?: string;
  duration?: string;
}

@Component({
  selector: 'app-carga-partidas-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './load-games-page.component.html',
  styleUrl: './load-games-page.component.css'
})
export class LoadGamesPageComponent implements OnInit {

  games: SavedGame[] = [];
  filteredGames: SavedGame[] = [];
  loading = true;
  error = '';

  textFilter = '';
  stateFilter = 'todos';
  playersFiltered = 'todos';
  orderBy = 'fecha-desc';

  availableArmies = [
    { value: 'todos', label: 'Todos los estados' },
    { value: GameStatus.IN_PROGRESS, label: 'En progreso' },
    { value: GameStatus.PAUSED, label: 'Pausadas' },
    { value: GameStatus.FINISHED, label: 'Terminadas' }
  ];

  showModal = false;
  gameToRemove: SavedGame | null = null;

  constructor(
    private router: Router,
    private gameService: GameService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadGames();
  }

  async loadGames(): Promise<void> {
    try {
      this.loading = true;
      this.error = '';

      // Simular datos mientras no esté la API completa
      await this.loadSimulatedGames();

      // TODO - Implementar esto
      // Cuando esté la API real, usar esto:
      // const games = await this.gameService.getAllGames().toPromise();
      // this.processGames(games);

      this.applyFilters();
    } catch (error) {
      this.error = 'Error al cargar las partidas guardadas';
      console.error('Error cargando partidas:', error);
    } finally {
      this.loading = false;
    }
  }

  private async loadSimulatedGames(): Promise<void> {
    await new Promise(resolve => setTimeout(resolve, 1000));

    this.games = [
      {
        id: 1,
        name: 'Conquista Mundial #1',
        creationDate: '2025-01-15T10:30:00Z',
        lastPlayDate: '2025-01-20T15:45:00Z',
        players: ['TuNombre', 'Bot_Experto', 'Amigo123'],
        progress: 65,
        state: GameStatus.IN_PROGRESS,
        duration: '2h 15m'
      },
      {
        id: 2,
        name: 'Batalla Épica',
        creationDate: '2025-01-10T14:20:00Z',
        lastPlayDate: '2025-01-18T20:10:00Z',
        players: ['TuNombre', 'Estratega', 'Conquistador', 'Bot_Balanceado'],
        progress: 45,
        state: GameStatus.PAUSED,
        duration: '1h 30m'
      },
      {
        id: 3,
        name: 'Victoria Rápida',
        creationDate: '2025-01-05T09:15:00Z',
        lastPlayDate: '2025-01-05T10:45:00Z',
        players: ['TuNombre', 'Bot_Novato', 'Bot_Novato'],
        progress: 100,
        state: GameStatus.FINISHED,
        duration: '45m'
      }
    ];
  }

  applyFilters(): void {
    let result = [...this.games];

    if (this.textFilter.trim()) {
      const text = this.textFilter.toLowerCase();
      result = result.filter(p =>
        p.name.toLowerCase().includes(text) ||
        p.players.some(j => j.toLowerCase().includes(text))
      );
    }

    if (this.stateFilter !== 'todos') {
      result = result.filter(p => p.state === this.stateFilter);
    }

    if (this.playersFiltered !== 'todos') {
      const numberPlayers = parseInt(this.playersFiltered);
      result = result.filter(p => p.players.length === numberPlayers);
    }

    result.sort((a, b) => {
      switch (this.orderBy) {
        case 'fecha-desc':
          return new Date(b.lastPlayDate).getTime() - new Date(a.lastPlayDate).getTime();
        case 'fecha-asc':
          return new Date(a.lastPlayDate).getTime() - new Date(b.lastPlayDate).getTime();
        case 'nombre':
          return a.name.localeCompare(b.name);
        case 'progreso':
          return b.progress - a.progress;
        default:
          return 0;
      }
    });

    this.filteredGames = result;
  }

  loadGame(game: SavedGame): void {
    if (game.state === GameStatus.FINISHED) {
      alert('Esta partida ya terminó. No se puede cargar.');
      return;
    }

    console.log('Cargando partida:', game);
    // TODO - Implementar esto
    // Aquí iría la lógica para cargar la partida
    // this.gameService.loadState(game.id).subscribe({...});

    // Por ahora, simular navegación al juego
    this.router.navigate(['/game'], { queryParams: { gameId: game.id } });
  }

  // Eliminar partida
  confirmRemove(game: SavedGame): void {
    this.gameToRemove = game;
    this.showModal = true;
  }

  deleteGame(): void {
    if (!this.gameToRemove) return;

    const index = this.games.findIndex(p => p.id === this.gameToRemove!.id);
    if (index > -1) {
      this.games.splice(index, 1);
      this.applyFilters();
    }

    this.openModal();
  }

  openModal(): void {
    this.showModal = false;
    this.gameToRemove = null;
  }

  getStateIcon(state: GameStatus): string {
    switch (state) {
      case GameStatus.IN_PROGRESS: return '🎮';
      case GameStatus.PAUSED: return '⏸️';
      case GameStatus.FINISHED: return '🏆';
      default: return '❓';
    }
  }

  getStateColor(state: GameStatus): string {
    switch (state) {
      case GameStatus.IN_PROGRESS: return '#22c55e';
      case GameStatus.PAUSED: return '#f59e0b';
      case GameStatus.FINISHED: return '#3b82f6';
      default: return '#6b7280';
    }
  }

  formatDate(date: string): string {
    const date1 = new Date(date);
    const now = new Date();
    const diffTime = Math.abs(now.getTime() - date1.getTime());
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

    if (diffDays === 1) return 'Ayer';
    if (diffDays < 7) return `Hace ${diffDays} días`;
    return date1.toLocaleDateString('es-ES');
  }

  trackByGame(index: number, game: SavedGame): number {
    return game.id;
  }

  trackByPlayer(index: number, player: string): string {
    return player;
  }

  goToLobby(): void {
    this.router.navigate(['/lobby']);
  }

  cretaeNewGame(): void {
    this.router.navigate(['/game-config']);
  }
}

