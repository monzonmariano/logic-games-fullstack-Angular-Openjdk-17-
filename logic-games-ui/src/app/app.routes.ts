import { Routes } from '@angular/router';

// 1. Importa los dos componentes nuevos
import { Login } from './pages/login/login';
import { Register } from './pages/register/register';
import { Home } from './pages/home/home';
import { authGuard } from './guards/auth-guard';
import { ForgotPassword } from './pages/forgot-password/forgot-password';
import { ResetPassword } from './pages/reset-password/reset-password';
import { SudokuBoard } from './pages/play/sudoku-board/sudoku-board';
import { Scoreboard } from './pages/scoreboard/scoreboard';
import { SudokuLobby } from './sudoku-lobby/sudoku-lobby';

export const routes: Routes = [


    // Si la URL está vacía (ej. localhost:4200/)
    {
        path: '',
        component: Home, // Carga el HomeComponent
        canActivate: [authGuard]
    },
    {
        path: 'play/sudoku', // Esta es la ruta a la que 'home.ts' te envía
        component: SudokuBoard,
        canActivate: [authGuard] // ¡También protegida!
    },
    // 2. Añade la ruta para el Login
    {
        path: 'login', // Cuando la URL sea /login...
        component: Login // ...carga el LoginComponent
    },
    // 3. Añade la ruta para el Registro
    {
        path: 'register', // Cuando la URL sea /register...
        component: Register // ...carga el RegisterComponent
    },

    {
        path: 'forgot-password',
        component: ForgotPassword
    },
    // Esta página recibirá el token de la URL
    {
        path: 'reset-password',
        component: ResetPassword
    },
    // --- RUTA DE HISTORIAL! ---
  {
    path: 'scoreboard', // La URL será /scoreboard
    component: Scoreboard,
    canActivate: [authGuard] // ¡Protegida, por supuesto!
  },
  { 
    path: 'sudoku-lobby', 
    component: SudokuLobby, 
    canActivate: [authGuard] // <-- Protegida
  },


];