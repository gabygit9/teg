import { Component, OnInit } from '@angular/core';
import { ModalMessageService } from '../../core/services/modal.service';

@Component({
  selector: 'app-modal-message',
  templateUrl: './modal-message.component.html',
  styleUrls: ['./modal-message.component.css'] // corregí styleUrls
})
export class ModalMessageComponent implements OnInit {
  showModal = false;
  message = '';
  title = '';

  constructor(private modalService: ModalMessageService) {}

  ngOnInit(): void {
    this.modalService.message$.subscribe(data => {
      if (data) {
        this.message = data.message;
        this.title = data.title ?? '';
        this.showModal = true;
      } else {
        this.showModal = false;
      }
    });
  }

  closeModal() {
    this.showModal = false;
    this.modalService.close();
  }
}
