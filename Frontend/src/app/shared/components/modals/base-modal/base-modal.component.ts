import { Component, Input, Output, EventEmitter, OnInit, OnDestroy, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';

export type ModalSize = 'small' | 'medium' | 'large' | 'extra-large';
export type ModalPosition = 'center' | 'top' | 'bottom';

@Component({
  selector: 'app-base-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './base-modal.component.html',
  styleUrl: './base-modal.component.css'
})
export class BaseModalComponent implements OnInit, OnDestroy {

  @Input() isOpen: boolean = false;
  @Input() title: string = '';
  @Input() size: ModalSize = 'medium';
  @Input() position: ModalPosition = 'center';
  @Input() showCloseButton: boolean = true;
  @Input() closableOnBackdrop: boolean = true;
  @Input() closableOnEscape: boolean = true;
  @Input() showHeader: boolean = true;
  @Input() showFooter: boolean = false;
  @Input() customClass: string = '';
  @Input() blurBackground: boolean = true;
  @Input() preventScroll: boolean = true;

  @Output() onOpen = new EventEmitter<void>();
  @Output() onClose = new EventEmitter<void>();
  @Output() onBackdropClick = new EventEmitter<void>();

  private originalBodyOverflow: string = '';

  ngOnInit(): void {
    if (this.isOpen) {
      this.handleOpen();
    }
  }

  ngOnDestroy(): void {
    this.restoreBodyScroll();
  }

  ngOnChanges(): void {
    if (this.isOpen) {
      this.handleOpen();
    } else {
      this.handleClose();
    }
  }

  @HostListener('document:keydown.escape', ['$event'])
  onEscapeKey(event: KeyboardEvent): void {
    if (this.isOpen && this.closableOnEscape) {
      this.closeModal();
    }
  }

  private handleOpen(): void {
    if (this.preventScroll) {
      this.preventBodyScroll();
    }
    this.onOpen.emit();
  }

  private handleClose(): void {
    if (this.preventScroll) {
      this.restoreBodyScroll();
    }
  }

  closeModal(): void {
    this.onClose.emit();
  }

  onBackdropClickHandler(event: MouseEvent): void {
    this.onBackdropClick.emit();
    if (this.closableOnBackdrop) {
      this.closeModal();
    }
  }

  onModalContentClick(event: MouseEvent): void {
    event.stopPropagation();
  }

  private preventBodyScroll(): void {
    this.originalBodyOverflow = document.body.style.overflow;
    document.body.style.overflow = 'hidden';
  }

  private restoreBodyScroll(): void {
    document.body.style.overflow = this.originalBodyOverflow;
  }

  get modalClasses(): string {
    const classes = [
      'base-modal',
      `base-modal--${this.size}`,
      `base-modal--${this.position}`
    ];

    if (this.customClass) classes.push(this.customClass);
    if (this.blurBackground) classes.push('base-modal--blur');

    return classes.join(' ');
  }

  get backdropClasses(): string {
    const classes = ['modal-backdrop'];

    if (this.blurBackground) classes.push('modal-backdrop--blur');

    return classes.join(' ');
  }
}
