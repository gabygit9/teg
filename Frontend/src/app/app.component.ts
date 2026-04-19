import { Component } from '@angular/core';
import {RouterOutlet} from '@angular/router';
import {ModalMessageComponent} from './features/modal-message/modal-message.component';
import {ToastNotificationComponent} from './shared/components/toast-notification/toast-notification.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, ModalMessageComponent, ToastNotificationComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
}
