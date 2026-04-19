import {Component, inject, OnDestroy, OnInit} from '@angular/core';
import {CommonModule, NgOptimizedImage} from '@angular/common';
import {AuthService} from '../../../core/services/auth.service';
import {Subscription} from 'rxjs';
import {FormsModule, NgForm} from '@angular/forms';
import {requestUserDTO} from '../../../core/models/interfaces/ResponseUserDTO';
import {Router, RouterLink} from '@angular/router';
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
          this.userName = this.authService.getUserNameFromToken() || "Usuario";
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
    this.modalService.info('¿Seguro que querés cerrar la sesión?', 'Confirmar cierre', 0);
    // Para un confirm verdadero, vamos a usar una modal personalizada
    // Por ahora mantenemos el confirm del navegador, pero lo mejoraremos después
    setTimeout(() => {
      const confirmation = confirm('¿Seguro que querés cerrar la sesión?');
      if (confirmation) {
        this.authService.logout();
        this.modalService.success('Sesión cerrada correctamente', 'Hasta luego');
        setTimeout(() => {
          this.router.navigate(["/login"]);
        }, 1500);
      }
    }, 100);
  }

  onSubmit(login: NgForm) {
    if (login.invalid) {
      this.modalService.info('Por favor, completa todos los campos requeridos', 'Formulario incompleto');
      return;
    }

    this.subscription = this.authService.loginUser(this.userModel).subscribe({
      next: (res) => {
        if (res && res.token) {
          this.authService.setSession(res.token);
          this.modalService.success('¡Bienvenido! Redirigiendo al lobby...', 'Inicio de sesión exitoso');
          setTimeout(() => {
            this.router.navigate(["/lobby"]);
          }, 2500);
        } else {
          this.modalService.error('No se recibió token de autenticación', 'Error de autenticación');
        }
      },
      error: () => {
        this.modalService.error('Verifica tu correo y contraseña', 'No se pudo iniciar sesión');
      }
    });

  }

  irAlLobby(): void {
    this.router.navigate(['/lobby']);
  }
}
