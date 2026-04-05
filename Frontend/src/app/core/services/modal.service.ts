import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

interface ModalData {
  message: string;
  title?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ModalMessageService {
  private messageSubject = new BehaviorSubject<ModalData | null>(null);
  message$ = this.messageSubject.asObservable();

  //Es una funcion que espera como parametros dos strings, un mensaje y un titulo, solo que el titulo es opcional
  modalMessage(message: string, title: string = '') {
    this.messageSubject.next({ message, title });
  }

  close() {
    this.messageSubject.next(null);
  }
}
