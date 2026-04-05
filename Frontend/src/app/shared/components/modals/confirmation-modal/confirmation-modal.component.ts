import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BaseModalComponent } from '../base-modal/base-modal.component';
import { BaseButtonComponent } from '../../buttons/base-button/base-button.component';

export type ConfirmationType = 'danger' | 'warning' | 'info' | 'success';

@Component({
  selector: 'app-confirmation-modal',
  standalone: true,
  imports: [CommonModule, BaseModalComponent, BaseButtonComponent],
  templateUrl: './confirmation-modal.component.html',
  styleUrl: './confirmation-modal.component.css'
})
export class ConfirmationModalComponent {

  @Input() isOpen: boolean = false;
  @Input() title: string = 'Confirmar Acción';
  @Input() message: string = '¿Estás seguro de que quieres realizar esta acción?';
  @Input() type: ConfirmationType = 'warning';
  @Input() confirmText: string = 'Confirmar';
  @Input() cancelText: string = 'Cancelar';
  @Input() showIcon: boolean = true;
  @Input() loading: boolean = false;
  @Input() destructive: boolean = false; // Para acciones destructivas como eliminar

  @Output() onConfirm = new EventEmitter<void>();
  @Output() onCancel = new EventEmitter<void>();
  @Output() onClose = new EventEmitter<void>();

  closeModal(): void {
    this.onClose.emit();
  }

  confirm(): void {
    this.onConfirm.emit();
  }

  cancel(): void {
    this.onCancel.emit();
  }

  get icon(): string {
    switch (this.type) {
      case 'danger': return '⚠️';
      case 'warning': return '🔔';
      case 'info': return 'ℹ️';
      case 'success': return '✅';
      default: return '❓';
    }
  }

  get confirmButtonVariant(): 'primary' | 'danger' | 'warning' | 'success' {
    if (this.destructive) return 'danger';

    switch (this.type) {
      case 'danger': return 'danger';
      case 'warning': return 'warning';
      case 'info': return 'primary';
      case 'success': return 'success';
      default: return 'primary';
    }
  }

  get containerClass(): string {
    return `confirmation-modal confirmation-modal--${this.type}`;
  }
}
