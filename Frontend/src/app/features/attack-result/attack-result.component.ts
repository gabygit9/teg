import {Component, EventEmitter, Input, Output, signal} from '@angular/core';
import {AttackResultDto} from '../../core/models/interfaces/AttackDto';

@Component({
  selector: 'app-attack-result',
  imports: [],
  templateUrl: './attack-result.component.html',
  styleUrl: './attack-result.component.css'
})
export class AttackResultComponent {
  @Input() result?: AttackResultDto;
  @Output() modalClose = new EventEmitter<void>();
  showModal = signal(false);

  openModal(result: AttackResultDto) {
    this.result = result;
    this.showModal.set(true);
  }

  closeModal() {
    this.showModal.set(false);
    this.modalClose.emit();
  }
}
