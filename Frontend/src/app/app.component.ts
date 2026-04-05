import { Component } from '@angular/core';
import {RouterOutlet} from '@angular/router';
import {ModalMessageComponent} from './features/modal-message/modal-message.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, ModalMessageComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
}
