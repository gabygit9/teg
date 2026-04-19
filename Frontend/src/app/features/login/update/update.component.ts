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
      console.log('Token decodificado:', decoded); // Debug: ver qué contiene el token

      this.userId = decoded.id;

      // Asignar al placeholder
      this.namePlaceholder = decoded.nombre || '';
      this.emailPlaceholder = decoded.sub || '';

      // IMPORTANTE: También asignar a userModel para que se vea en los inputs
      this.userModel.name = decoded.nombre || '';
      this.userModel.email = decoded.sub || '';

      console.log('namePlaceholder:', this.namePlaceholder); // Debug
      console.log('emailPlaceholder:', this.emailPlaceholder); // Debug
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

    // Validar que al menos uno de los campos fue modificado
    if (!this.userModel.name && !this.userModel.email && !this.userModel.password) {
      alert('Debes cambiar al menos uno de tus datos');
      return;
    }

    // Construir el DTO solo con los campos que fueron modificados
    const userDTO: any = {};

    if (this.userModel.name) {
      userDTO.name = this.userModel.name;
    }
    if (this.userModel.email) {
      userDTO.email = this.userModel.email;
    }
    if (this.userModel.password) {
      userDTO.password = this.userModel.password;
    }

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
        console.error('Status:', err.status);
        console.error('Message:', err.message);
        console.error('Error response:', err.error);

        // Mostrar el error específico del backend
        const errorMessage = err.error?.message || err.error?.error || 'Error al actualizar los datos.';
        alert(`Error: ${errorMessage}`);
      }
    });
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }
}
