import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {CommonModule, NgClass} from '@angular/common';
import {PlayerCardSimple} from '../../core/models/interfaces/PlayerCard';
import {CardService} from '../../core/services/card.service';

@Component({
  selector: 'app-cards-modal',
  imports: [
    NgClass,
    CommonModule,
  ],
  templateUrl: './cards-modal.component.html',
  styleUrl: './cards-modal.component.css'
})
export class CardsModalComponent implements OnInit {
  @Input() cards: PlayerCardSimple[] = [];
  @Input() playerId!: number;
  @Input() gameId!: number;
  @Output() close = new EventEmitter<void>();
  @Output() requestedArmies = new EventEmitter<void>();

  successMessage = '';
  countryNotPossessed = '';
  showAlert = false;
  cardSelectedId?: number;
  selectedCards: number[]= [];

  constructor(private cardService: CardService) {}

  ngOnInit(): void {
    this.loadPlayerCards();
  }


  getSymbolImage(symbol: string): string {
    switch (symbol?.toLowerCase()) {
      case 'infantry': return 'assets/images/z_globo.png';
      case 'chivalry': return 'assets/images/z_galeon.png';
      case 'artillery': return 'assets/images/z_canon.png';
      default: return 'assets/images/z_globo.png';
    }
  }

  selectCard(id: number){
    const index = this.selectedCards.indexOf(id);

    if(index !== -1){
      this.selectedCards.splice(index, 1); //ya está seleccionada, deseleccionar
    } else if (this.selectedCards.length < 3){
      this.selectedCards.push(id);
    }
  }

  cardsById(id:number): PlayerCardSimple | undefined{
    return this.cards.find(t => t.id === id);
  }

  requestArmy() {
    if(!this.gameId || !this.playerId || !this.cardSelectedId) return;
    console.log("JugadorPartidaId correcto:", this.playerId)

    console.log("Tarjeta seleccionada:", this.cards.find(c => c.id === this.cardSelectedId));
    console.log("Solicitud enviada: ", {
      gameId: this.gameId,
      playerId: this.playerId,
      cardId: this.cardSelectedId
    });


    this.cardService.requestArmiesByCard(this.gameId, this.playerId, this.cardSelectedId).subscribe({
      next: () => {
        console.log('Ejércitos otorgados correctamente.');
        const usedCard = this.cards.find(c => c.id === this.cardSelectedId);
        if(usedCard){
          usedCard.used = true;
          console.log("Después de marcar como usada:", JSON.stringify(this.cards));
        }

        this.successMessage = '+2 ejércitos agregados'
        this.cardSelectedId = undefined;

        setTimeout(() => {
          this.successMessage = '';
        }, 3000);
        this.requestedArmies.emit();
      },
      error: (error) => {
        console.error("Respuesta de error:", error);
        if(error.status === 400) {
          this.showAlert = true;
          this.countryNotPossessed = error.error || 'desconocido';
          console.log("Pais no poseido:", this.countryNotPossessed);
        } else{
          console.error("Error inesperado al solicitar ejércitos", error)
        }

      }
    })
  }

  allUsed(): boolean {
    return this.cards.every(card => card.used);
  }


  closeAlert() {
    this.showAlert = false;
    this.countryNotPossessed = '';
  }

  doExchange() {
    if (this.selectedCards.length !== 3) return;

    this.cardService.doExchange(this.gameId, this.playerId, this.selectedCards)
      .subscribe({
        next: (success: any) => {
          if (success) {
            this.successMessage = '¡Canje realizado con éxito!';
            this.loadPlayerCards();
          } else {
            this.successMessage = 'Las tarjetas seleccionadas no forman una combinación válida.';
          }
        },
        error: () => {
          this.successMessage = 'Error al intentar realizar el canje.';
        }
      });
  }

  private loadPlayerCards() {
    this.cardService.getPlayerCards(this.playerId).subscribe(cards => {
      this.cards = cards;
      this.selectedCards = [];  // Resetear selección

      if (this.cards.length >= 6) {
        this.successMessage = 'Tienes 6 tarjetas. Debes realizar un canje antes de continuar.';
      }
    });
  }

}
//VER CANJEAR TARJETAS
