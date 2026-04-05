import {Component, inject, OnDestroy, OnInit} from '@angular/core';
import {FormsModule, NgForm} from '@angular/forms';
import {NgOptimizedImage} from '@angular/common';
import {Router, RouterLink} from '@angular/router';
import {Subscription} from 'rxjs';
import {AuthService} from '../../../core/services/auth.service';
import {Rol} from '../../../core/enums/Rol';

@Component({
  selector: 'app-register',
  imports: [
    FormsModule,
    NgOptimizedImage,
    RouterLink
  ],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent implements OnInit, OnDestroy{

  router: Router = inject(Router);

  authService: AuthService = inject(AuthService);

  subscription: Subscription = new Subscription();

  userModel = {
    name: "",
    email: "",
    password: "",
    confirmPassword: "",
  }

  ngOnInit(): void {
    const token = this.authService.getToken();
    if (token) {
      alert('Debes cerrar la sesión actual para registrar un nuevo usuario.');
      this.router.navigate(['/login']);
    }
  }

  onSubmit(register: NgForm) {
    if (register.invalid) {
      alert("Complete el registro");
      return;
    }
    if (this.userModel.password !== this.userModel.confirmPassword) {
      alert("Contraseñas no coinciden");
      return;
    }

    const userDTO = {
      name: this.userModel.name,
      email: this.userModel.email,
      password: this.userModel.password,
    }
    this.subscription = this.authService.registerUser(userDTO).subscribe({
      next:(res)=> {
        if (res.id > 0) {
          this.router.navigate(["/login"])
          alert("Usuario creado con éxito")
        } else {
          alert("Válide los datos del registro");
        }
      },
      error:()=> {
        alert("No pudo registrarse");
      }
    })

  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

}
