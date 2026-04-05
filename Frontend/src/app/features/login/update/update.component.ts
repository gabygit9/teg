import { FormsModule, NgForm } from '@angular/forms';
import { NgOptimizedImage } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { AuthService } from '../../../core/services/auth.service';
import { Subscription } from 'rxjs';
import { jwtDecode } from 'jwt-decode';

@Component({
  standalone: true,
  selector: 'app-update',
  imports: [
    FormsModule,
    NgOptimizedImage,
    RouterLink
  ],
  templateUrl: './update.component.html',
  styleUrls: ['./update.component.css']
})
export class UpdateComponent implements OnInit, OnDestroy {
  router: Router = inject(Router);
  authService: AuthService = inject(AuthService);
  subscription: Subscription = new Subscription();

  userModel = {
    name: '',
    email: '',
    password: '',
    confirmPassword: ''
  };

  namePlaceholder = '';
  emailPlaceholder = '';
  userId: number | null = null;

  ngOnInit(): void {
    const token = this.authService.getToken();

    if (!token) {
      alert('Debes iniciar sesión para actualizar tus datos.');
      this.router.navigate(['/login']);
      return;
    }

    try {
      const decoded: any = jwtDecode(token);
      this.userId = decoded.id;
      this.namePlaceholder = decoded.nombre || '';
      this.emailPlaceholder = decoded.sub || '';
    } catch (error) {
      alert('Error con el token. Iniciá sesión nuevamente.');
      this.router.navigate(['/login']);
    }
  }

  onSubmit(update: NgForm) {
    if (this.userModel.password !== this.userModel.confirmPassword) {
      alert('Las contraseñas no coinciden');
      return;
    }

    const userDTO = {
      name: this.userModel.name,
      email: this.userModel.email,
      password: this.userModel.password
    };

    if (this.userId === null) {
      alert('Usuario no identificado.');
      return;
    }

    this.subscription = this.authService.updateUser(this.userId, userDTO).subscribe({
      next: (res) => {
        alert('Datos actualizados correctamente. Debes volver a iniciar sesión.');
        this.authService.logout();
        this.router.navigate(['/login']);
      },
      error: (err) => {
        console.error('Error en update:', err);
        alert('Error al actualizar los datos.');
      }
    });
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }
}
