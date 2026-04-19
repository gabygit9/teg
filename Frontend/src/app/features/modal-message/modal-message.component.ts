import { Component, OnInit, OnDestroy } from '@angular/core';
import { ModalMessageService } from '../../core/services/modal.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-modal-message',
  templateUrl: './modal-message.component.html',
  styleUrls: ['./modal-message.component.css']
})
export class ModalMessageComponent implements OnDestroy {
  showModal = false;
  message = '';
  title = '';
  private subscription: Subscription | null = null;

  constructor(private modalService: ModalMessageService) {}

  ngOnDestroy(): void {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }

  closeModal() {
    this.showModal = false;
    this.modalService.close();
  }
}
