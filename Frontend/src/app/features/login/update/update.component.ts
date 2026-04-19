import { FormsModule, NgForm } from '@angular/forms';
import { NgOptimizedImage } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { AuthService } from '../../../core/services/auth.service';
import { ModalMessageService } from '../../../core/services/modal.service';
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
  modalService: ModalMessageService = inject(ModalMessageService);
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
      this.modalService.info('Debes iniciar sesión para actualizar tus datos', 'Acceso requerido');
      this.router.navigate(['/login']);
      return;
    }

    try {
      const decoded: any = jwtDecode(token);

      this.userId = decoded.id;
      this.namePlaceholder = decoded.nombre || '';
      this.emailPlaceholder = decoded.sub || '';
      this.userModel.name = decoded.nombre || '';
      this.userModel.email = decoded.sub || '';
    } catch (error) {
      this.modalService.error('Error con el token. Por favor, inicia sesión nuevamente', 'Error de autenticación');
      this.router.navigate(['/login']);
    }
  }

  onSubmit(update: NgForm) {
    if (this.userModel.password !== this.userModel.confirmPassword) {
      this.modalService.error('Las contraseñas no coinciden', 'Validación fallida');
      return;
    }

    if (!this.userModel.name && !this.userModel.email && !this.userModel.password) {
      this.modalService.info('Debes cambiar al menos uno de tus datos', 'Sin cambios');
      return;
    }

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
      this.modalService.error('Usuario no identificado', 'Error');
      return;
    }

    this.subscription = this.authService.updateUser(this.userId, userDTO).subscribe({
      next: (res) => {
        this.modalService.success('Datos actualizados correctamente. Redirigiendo a login...', 'Actualización exitosa');
        this.authService.logout();
        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 2000);
      },
      error: (err) => {
        const errorMessage = err.error?.message || err.error?.error || 'Error al actualizar los datos';
        this.modalService.error(errorMessage, 'Error en la actualización');
      }
    });
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }
}
