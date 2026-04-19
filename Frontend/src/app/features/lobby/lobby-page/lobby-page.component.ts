import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {SavedGames} from '../../../core/models/interfaces/SavedGames';
import {MementoService} from '../../../core/services/memento.service';
import {ModalMessageService} from '../../../core/services/modal.service';


@Component({
  selector: 'app-lobby-page',
  imports: [],
  templateUrl: './lobby-page.component.html',
  styleUrl: './lobby-page.component.css'
})
export class LobbyPageComponent implements OnInit {

  games: SavedGames[] = [];
  selectedGameId: number | null = null;

  constructor(private router: Router,
              private mementoService: MementoService,
              private modalService: ModalMessageService
  ) {
  }

  ngOnInit(): void {
    this.mementoService.getSavedGames().subscribe({
      next: (data) => {
        this.games = data;
      },
      error: (err) => {
        console.error('Error al cargar partidas guardadas:', err);
        this.modalService.error('No se pudieron cargar las partidas guardadas', 'Error al cargar');
      }
    });
  }

  selectGame(id: number) {
    if (this.selectedGameId === id) {
      this.selectedGameId = null;
    } else {
      this.selectedGameId = id;
    }
  }

  goToTheMap() {
    if (this.selectedGameId === null) {
      this.modalService.info('Por favor, selecciona una partida para cargar', 'Partida requerida');
      return;
    }

    this.mementoService.getLastState(this.selectedGameId).subscribe({
      next: (lastMemento) => {
        const mementoId = lastMemento.mementoId;
        if (!mementoId) {
          this.modalService.error('No se encontró el estado de la partida', 'Error de carga');
          return;
        }

        this.mementoService.restoreMemento(mementoId).subscribe({
          next: (restoredState) => {
            sessionStorage.setItem('estadoRestored', JSON.stringify(restoredState));
            sessionStorage.setItem('selectedGameId', this.selectedGameId!.toString());
            this.modalService.success('Cargando partida...', 'Listo');
            setTimeout(() => {
              this.router.navigate(['/game']);
            }, 500);
          },
          error: (err) => {
            console.error('Error al restaurar memento:', err);
            this.modalService.error('No se pudo restaurar la partida guardada', 'Error al cargar');
          }
        });
      },
      error: (err) => {
        console.error('Error al obtener el último memento:', err);
        this.modalService.error('No se pudo obtener el estado guardado', 'Error al cargar');
      }
    });
  }

  loadGameConfig() {
    this.router.navigate(['/game-config']);
  }

  goLogin() {
    this.router.navigate(['/login']);
  }

  goHome() {
    this.router.navigate(['/']);
  }
}
