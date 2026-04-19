import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export interface Toast {
  id: string;
  message: string;
  type: 'success' | 'error' | 'info';
  title?: string;
  duration?: number; // en milisegundos, 0 = sin auto-cerrar
}

@Injectable({
  providedIn: 'root'
})
export class ModalMessageService {
  private toastsSubject = new BehaviorSubject<Toast[]>([]);
  toasts$ = this.toastsSubject.asObservable();

  constructor() {}

  /**
   * Mostrar notificación de éxito
   */
  success(message: string, title: string = 'Éxito', duration: number = 3000): void {
    this.show('success', message, title, duration);
  }

  /**
   * Mostrar notificación de error
   */
  error(message: string, title: string = 'Error', duration: number = 4000): void {
    this.show('error', message, title, duration);
  }

  /**
   * Mostrar notificación informativa
   */
  info(message: string, title: string = 'Información', duration: number = 3000): void {
    this.show('info', message, title, duration);
  }

  /**
   * Método genérico para mostrar toast
   */
  private show(type: 'success' | 'error' | 'info', message: string, title: string = '', duration: number = 3000): void {
    const id = Date.now().toString();
    const toast: Toast = {
      id,
      message,
      type,
      title: title || this.getDefaultTitle(type),
      duration
    };

    const currentToasts = this.toastsSubject.value;
    this.toastsSubject.next([...currentToasts, toast]);

    // Auto-cerrar si tiene duración
    if (duration > 0) {
      setTimeout(() => {
        this.remove(id);
      }, duration);
    }
  }

  /**
   * Remover un toast específico
   */
  remove(id: string): void {
    const currentToasts = this.toastsSubject.value;
    this.toastsSubject.next(currentToasts.filter(t => t.id !== id));
  }

  /**
   * Obtener título por defecto según el tipo
   */
  private getDefaultTitle(type: 'success' | 'error' | 'info'): string {
    switch (type) {
      case 'success':
        return '✓ Éxito';
      case 'error':
        return '✕ Error';
      case 'info':
        return 'ℹ Información';
    }
  }

  /**
   * Compatibilidad con método antiguo (mantener para no romper código existente)
   */
  modalMessage(message: string, title: string = ''): void {
    this.info(message, title);
  }

  close(): void {
    this.toastsSubject.next([]);
  }
}
