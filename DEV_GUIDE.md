# LogicGames - Guía de Desarrollo y Arquitectura

Este documento contiene la arquitectura, los servicios de terceros y las instrucciones para levantar el entorno de desarrollo local sin romper producción.

## 🏗️ Arquitectura de Producción (La Nube)
El proyecto está distribuido en diferentes servicios gratuitos para maximizar el rendimiento y minimizar costos:

*   **Frontend (Angular):** Alojado en **Netlify**. Se despliega automáticamente con cada push a la rama `main`.
*   **Backend (Spring Boot):** Alojado en **Render** (Web Service). 
*   **Base de Datos (PostgreSQL):** Alojada en **NeonDB**. Es la base de datos real donde viven los usuarios de producción.
*   **Servicio de Correos:** **Resend** (vía JavaMailSender en Spring Boot).
*   **Keep-Alive (Cron):** **Cron-job.org** hace un ping a la API cada 10 minutos para evitar que Render suspenda el contenedor por inactividad.

## 💻 Entorno de Desarrollo Local (Localhost)
Para no ensuciar la base de datos de producción (Neon) ni gastar correos reales (Resend), el desarrollo local está 100% orquestado con **Docker**.

### Requisitos Previos
1. Tener Docker y `docker-compose` instalados en Ubuntu.
2. Crear un archivo llamado `.env` en la raíz del proyecto (este archivo está en el `.gitignore` por seguridad).

### Plantilla del archivo `.env`
Copia esto en tu `.env` local:
```env
# Base de Datos Local (Docker la crea automáticamente)
POSTGRES_USER=postgres
POSTGRES_PASSWORD=admin123
POSTGRES_DB=logicgames_local_db

# Seguridad de Spring Boot
APP_JWT_SECRET_KEY=VGhpcy1pcy1hLXN1cGVyLXNlY3JldC1rZXktZm9yLWxvZ2ljZ2FtZXMtYmFja2VuZA==

# URLs y Servicios
APP_FRONTEND_URL=http://localhost:8081
# Poner FAKE_PASSWORD activa el Modo Simulación: los emails se imprimen en la consola de Docker
APP_EMAIL_PASSWORD=FAKE_PASSWORD
