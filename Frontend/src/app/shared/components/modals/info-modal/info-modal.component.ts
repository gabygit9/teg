import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BaseModalComponent } from '../base-modal/base-modal.component';
import { BaseButtonComponent } from '../../buttons/base-button/base-button.component';

export type InfoType = 'info' | 'success' | 'warning' | 'error';

@Component({
  selector: 'app-info-modal',
  standalone: true,
  imports: [CommonModule, BaseModalComponent, BaseButtonComponent],
  templateUrl: './info-modal.component.html',
  styleUrl: './info-modal.component.css'
})
export class InfoModalComponent {

  @Input() isOpen: boolean = false;
  @Input() title: string = 'Información';
  @Input() message: string = '';
  @Input() type: InfoType = 'info';
  @Input() showIcon: boolean = true;
  @Input() buttonText: string = 'Entendido';
  @Input() size: 'small' | 'medium' | 'large' = 'medium';
  @Input() autoClose: boolean = false;
  @Input() autoCloseDelay: number = 3000; // 3 segundos

  @Output() onClose = new EventEmitter<void>();
  @Output() onButtonClick = new EventEmitter<void>();

  ngOnInit(): void {
    if (this.autoClose && this.isOpen) {
      setTimeout(() => {
        this.closeModal();
      }, this.autoCloseDelay);
    }
  }

  closeModal(): void {
    this.onClose.emit();
  }

  handleButtonClick(): void {
    this.onButtonClick.emit();
    this.closeModal();
  }

  get icon(): string {
    switch (this.type) {
      case 'info': return 'ℹ️';
      case 'success': return '✅';
      case 'warning': return '⚠️';
      case 'error': return '❌';
      default: return 'ℹ️';
    }
  }

  get buttonVariant(): 'primary' | 'success' | 'warning' | 'danger' {
    switch (this.type) {
      case 'info': return 'primary';
      case 'success': return 'success';
      case 'warning': return 'warning';
      case 'error': return 'danger';
      default: return 'primary';
    }
  }

  get containerClass(): string {
    return `info-modal info-modal--${this.type}`;
  }
}
