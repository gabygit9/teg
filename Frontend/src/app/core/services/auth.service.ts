import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { responseUserDTO } from '../models/interfaces/ResponseUserDTO';
import { jwtDecode } from 'jwt-decode';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private apiUrl = 'http://localhost:8080/api/v1/users';


  private loggedInSubject = new BehaviorSubject<boolean>(this.hasToken());
  loggedIn$ = this.loggedInSubject.asObservable();

  constructor(private http: HttpClient) {}

  registerUser(data: {
    name: string;
    email: string;
    password: string;
  }): Observable<responseUserDTO> {
    return this.http.post<responseUserDTO>(`${this.apiUrl}/register`, data);
  }

  loginUser(data: {
    email: string;
    password: string;
  }): Observable<{ token: string }> {
    return this.http.post<{ token: string }>(`${this.apiUrl}/login`, data);
  }

  updateUser(id: number, data: {
    name?: string;
    email?: string;
    password?: string;
  }): Observable<any> {
    const token = this.getToken();
    const headers = { Authorization: `Bearer ${token}` };

    return this.http.put<any>(
      `${this.apiUrl}/update/${id}`,
      data,
      { headers }
    );
  }

  logout(): void {
    localStorage.removeItem('jwt_token');
    this.loggedInSubject.next(false);  // aviso que cerró sesión
  }

  setSession(token: string): void {
    localStorage.setItem('jwt_token', token);
    this.loggedInSubject.next(true);   // aviso que inició sesión
  }

  getToken(): string | null {
    return localStorage.getItem('jwt_token');
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  private hasToken(): boolean {
    return !!this.getToken();
  }

  getUserNameFromToken(): string | null {
    const token = this.getToken();
    if (!token) return null;
    try {
      const payload: any = jwtDecode(token);
      return payload.nombre || null;
    } catch (e) {
      console.error('Error decoding JWT token', e);
      return null;
    }
  }

  getUserIdFromToken(): number | null {
    const token = this.getToken();
    if (!token) return null;
    try {
      const payload: any = jwtDecode(token);
      return payload.id || null;
    } catch (e) {
      console.error('Error decoding JWT token', e);
      return null;
    }
  }
}
