import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-inicio-page',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './home-page.component.html',
  styleUrl: './home-page.component.css'
})
export class HomePageComponent {

  constructor(
    private router: Router,
    private authService: AuthService
  ) {}

  goLogin(): void {
    this.router.navigate(['/login']);
  }

  goRegistry(): void {
    this.router.navigate(['/register']);
  }

  goLobby(): void {
    if (this.authService.isLoggedIn()) {
      this.router.navigate(['/lobby']);
    } else {
      this.goLogin();
    }
  }

  get isLogged(): boolean {
    return this.authService.isLoggedIn();
  }

  get userName(): string | null {
    return this.authService.getUserNameFromToken();
  }

  logOut(): void {
    const confirmation = confirm('¿Seguro que querés cerrar la sesión?');
    if (confirmation) {
      this.authService.logout();
      this.router.navigate(["/login"]);
    }
  }
}

