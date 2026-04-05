import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {SavedGames} from '../../../core/models/interfaces/SavedGames';
import {MementoService} from '../../../core/services/memento.service';
import {NgForOf} from '@angular/common';
import {ModalMessageService} from '../../../core/services/modal.service';


@Component({
  selector: 'app-lobby-page',
  imports: [
    NgForOf
  ],
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

  // en modo debug
  goToTheMap() {
    if (this.selectedGameId === null) {
      alert("Por favor, seleccione una partida primero.");
      return;
    }

    this.mementoService.getLastState(this.selectedGameId).subscribe({
      next: (lastMemento) => {
        console.log('Ultimo memento recibido:', lastMemento);

        const mementoId = lastMemento.mementoId;
        if (!mementoId) {
          // alert('No se encontró el id del memento para esta partida');
          this.modalService.modalMessage('No se encontró el id del memento para esta partida.', 'Partida');
          return;
        }

        this.mementoService.restoreMemento(mementoId).subscribe({
          next: (restoredState) => {
            sessionStorage.setItem('estadoRestored', JSON.stringify(restoredState));
            sessionStorage.setItem('selectedGameId', this.selectedGameId!.toString());
            this.router.navigate(['/game']);
          },
          error: (err) => {
            console.error('Error al restaurar memento:', err);
            // alert('No se pudo restaurar la partida guardada.');
            this.modalService.modalMessage('No se pudo restaurar la partida guardada.', 'Partida');

          }
        });
      },
      error: (err) => {
        console.error('Error al obtener el último memento:', err);
        // alert('No se pudo obtener el último estado guardado.');
        this.modalService.modalMessage('No se pudo obtener el último estado guardado.', 'Partida');

      }
    });
  }


  loadGameConfig() {
    this.router.navigate(['/game-config']);
  }

  goLogin() {
    this.router.navigate(['/login']);
  }

  goLoadGames() {
    this.router.navigate(['/load-games']);
  }

  goVictory() {
    this.router.navigate(['/victory'], {queryParams: {gameId: 1}});
  }

  goHome() {
    this.router.navigate(['/home']);
  }
}

