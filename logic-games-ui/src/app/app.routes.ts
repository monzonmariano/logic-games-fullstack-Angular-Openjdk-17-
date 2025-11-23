import { Routes } from '@angular/router';

// 1. Importa los dos componentes nuevos
import { Login } from './features/auth/login/login';
import { Register } from './features/auth/register/register';
import { Home } from './features/home/home';
import { authGuard } from './core/guards/auth-guard';
import { ForgotPassword } from './features/auth/forgot-password/forgot-password';
import { ResetPassword } from './features/auth/reset-password/reset-password';
import { SudokuBoard } from './features/games/sudoku/pages/board/sudoku-board';
import { Scoreboard } from './features/scoreboard/scoreboard';
import { SudokuLobby } from './features/games/sudoku/pages/lobby/sudoku-lobby';
import { VerifyEmail } from './features/auth/verify-email/verify-email';
import { VerifyLink } from './features/auth/verify-link/verify-link';
import { EnterResetCode } from './features/auth/enter-reset-code/enter-reset-code';
import { WordSearchLobby } from './features/games/wordsearch/pages/lobby/wordsearch-lobby';

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
    {
        path: 'verify-email', // La ruta a la que te redirige el registro
        component: VerifyEmail
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
    {
        path: 'verify-link', // La ruta del enlace del email
        component: VerifyLink
    },
    { path: 'enter-reset-code', component: EnterResetCode }
    ,// RUTA DE LOBBY SOPA DE LETRAS
    {
        path: 'wordsearch-lobby',
        component: WordSearchLobby,
        canActivate: [authGuard]
    },
    
    // RUTA DE JUEGO (TABLERO) - ¡PENDIENTE DE CREAR!
    {
        path: 'play/wordsearch',
        loadComponent: () => import('./features/games/wordsearch/pages/board/wordsearch-board')
            .then(m => m.WordSearchBoard),
        canActivate: [authGuard]
    }


];