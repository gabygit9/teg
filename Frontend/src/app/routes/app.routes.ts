import {Routes} from '@angular/router';
import {LoginComponent} from '../features/login/login/login.component';
import {RegisterComponent} from '../features/login/register/register.component';
import {LobbyPageComponent} from '../features/lobby/lobby-page/lobby-page.component';
import {MapComponent} from '../features/Juego/map/map.component';
import {AuthGuard} from '../core/guards/auth.guard';
import {UpdateComponent} from '../features/login/update/update.component';
import {GamePageComponent} from '../features/Juego/game-page/game-page.component';
import {GameConfigPageComponent} from '../features/GameConfig/game-config-page/game-config-page.component';
import {HomePageComponent} from '../features/inicio/inicio-page/home-page.component';
import {LoadGamesPageComponent} from '../features/carga-partidas/carga-partidas-page/load-games-page.component';
import {VictoryPageComponent} from '../features/victory-page/victory-page.component';


export const routes: Routes = [
  // Ruta por defecto - Página de Inicio
  { path: "", component: HomePageComponent },
  { path: "home", component: HomePageComponent },

  // Página de carga de partidas
  { path: "load-games", component: LoadGamesPageComponent, canActivate: [AuthGuard] },

  // Página de victoria
  { path: "victory", component: VictoryPageComponent, canActivate: [AuthGuard] },

  // Rutas de autenticación
  { path: "login", component: LoginComponent },
  { path: "register", component: RegisterComponent },
  { path: "update", component: UpdateComponent },

  // Rutas protegidas del juego
  { path: "lobby", component: LobbyPageComponent, canActivate: [AuthGuard] },
  { path: "map", component: MapComponent, canActivate: [AuthGuard] },
  { path: "game-config", component: GameConfigPageComponent, canActivate: [AuthGuard] },
  { path: "game", component: GamePageComponent, canActivate: [AuthGuard] },

  // Redirección de rutas no encontradas
  { path: "**", redirectTo: "" },

];
