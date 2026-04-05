import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Objective} from '../../../core/models/interfaces/Objective';
import {ObjectiveService} from '../../../core/services/objective.service';
import {CommonModule, NgClass} from '@angular/common';

@Component({
  selector: 'app-objetivo-modal',
  standalone: true,
  imports: [
    NgClass
  ],
  templateUrl: './objective-modal.component.html',
  styleUrl: './objective-modal.component.css'
})
export class ObjectiveModalComponent implements OnInit {
  @Input() objective!: Objective;
  @Input() achieved: boolean = false;

  @Output() close = new EventEmitter<void>();

  close_modal(){
    this.close.emit();
  }

  ngOnInit(): void {
    console.log('objetivo modal:', this.objective);
  }
}
//MUESTRA EL OBJETIVO ACTUAL DEL JUGADOR EN UNA VENTANA MODAL
