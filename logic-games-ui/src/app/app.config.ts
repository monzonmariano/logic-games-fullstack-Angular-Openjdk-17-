import { ApplicationConfig, provideBrowserGlobalErrorListeners, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { provideHttpClient , withInterceptors} from '@angular/common/http';
import { authInterceptor } from './interceptors/auth-interceptor';


export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
  
    // Le decimos al "Jefe de Aeropuerto" (HttpClient)
    // que "use los siguientes interceptores" (withInterceptors)
    // y le pasamos nuestro guardia.
    provideHttpClient(withInterceptors([
      authInterceptor
    ]))
  ]
};
