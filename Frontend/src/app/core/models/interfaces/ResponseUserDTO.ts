import {Rol} from '../../enums/Rol';

export interface responseUserDTO {
  id: number;
  name: string;
  email: string;
  password: string;
  rol: Rol;
}

export interface requestUserDTO {
  email: string;
  password: string;
}
