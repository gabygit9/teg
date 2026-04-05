import {Component, inject, OnDestroy, OnInit} from '@angular/core';
import {CommonModule, NgOptimizedImage} from '@angular/common';
import {AuthService} from '../../../core/services/auth.service';
import {Subscription} from 'rxjs';
import {FormsModule, NgForm} from '@angular/forms';
import {requestUserDTO} from '../../../core/models/interfaces/ResponseUserDTO';
import {Router, RouterLink} from '@angular/router';
import {jwtDecode} from 'jwt-decode';
import { ModalMessageService } from '../../../core/services/modal.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    NgOptimizedImage,
    FormsModule,
    RouterLink
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent implements OnInit, OnDestroy{

  router: Router = inject(Router);

  authService: AuthService = inject(AuthService);

  subscription: Subscription = new Subscription();

  userModel: requestUserDTO = {
    email: "",
    password: ""
  }

  isLoggedIn = false;
  userName = "";
  constructor(private modalService: ModalMessageService) {}

  ngOnInit(): void {
    this.subscription.add(
      this.authService.loggedIn$.subscribe(isLogged => {
        this.isLoggedIn = isLogged;
        if (isLogged) {
          const token = this.authService.getToken();
          if (token) {
            try {
              const decoded: any = jwtDecode(token);
              this.userName = decoded.name || "Usuario";
            } catch {
              this.userName = "Usuario";
            }
          }
        } else {
          this.userName = "";
        }
      })
    );
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  logOut(): void {
    const confirmation = confirm('¿Seguro que querés cerrar la sesión?');
    if (confirmation) {
      this.authService.logout();
      this.router.navigate(["/login"]);
    }
  }

  onSubmit(login: NgForm) {
    if (login.invalid) {
      // alert("Complete el inicio de sesión");
      this.modalService.modalMessage(`Complete el formulario`, 'Iniciar sesión');
      return;
    }

    this.subscription = this.authService.loginUser(this.userModel).subscribe({
      next: (res) => {
        if (res && res.token) {
          this.authService.setSession(res.token);
          // alert("Login exitoso");
          this.modalService.modalMessage(`Login exitoso`, 'Iniciar sesión');
          setTimeout(() => {
            this.router.navigate(["/lobby"]);
          }, 2500);
        } else {
          // alert("Token no recibido");
          this.modalService.modalMessage(`Token no recibido`, 'Iniciar sesión');
        }
      },
      error: () => {
        // alert("No se pudo iniciar sesión");
        this.modalService.modalMessage(`No se pudo iniciar sesión`, 'Iniciar sesión');
      }
    });

  }

  irAlLobby(): void {
    this.router.navigate(['/lobby']);
  }
}
