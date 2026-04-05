import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

export type ButtonVariant = 'primary' | 'secondary' | 'danger' | 'success' | 'warning' | 'outline' | 'ghost';
export type ButtonSize = 'small' | 'medium' | 'large';

@Component({
  selector: 'app-base-button',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './base-button.component.html',
  styleUrl: './base-button.component.css'
})
export class BaseButtonComponent {

  @Input() variant: ButtonVariant = 'primary';
  @Input() size: ButtonSize = 'medium';
  @Input() disabled: boolean = false;
  @Input() loading: boolean = false;
  @Input() fullWidth: boolean = false;
  @Input() icon: string = '';
  @Input() iconPosition: 'left' | 'right' = 'left';
  @Input() type: 'button' | 'submit' | 'reset' = 'button';

  @Output() onClick = new EventEmitter<MouseEvent>();

  handleClick(event: MouseEvent): void {
    if (!this.disabled && !this.loading) {
      this.onClick.emit(event);
    }
  }

  get buttonClasses(): string {
    const classes = [
      'base-button',
      `base-button--${this.variant}`,
      `base-button--${this.size}`
    ];

    if (this.disabled) classes.push('base-button--disabled');
    if (this.loading) classes.push('base-button--loading');
    if (this.fullWidth) classes.push('base-button--full-width');
    if (this.icon) classes.push('base-button--with-icon');

    return classes.join(' ');
  }
}
