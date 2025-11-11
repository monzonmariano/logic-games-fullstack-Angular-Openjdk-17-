# 🎮 LogicGames - Aplicación Full-Stack de Juegos de Lógica

¡Bienvenido! Este es un proyecto de portafolio de una aplicación web full-stack, construida desde cero, que sirve juegos de lógica como el Sudoku. Toda la aplicación está 100% contenedorizada con Docker.

## ✨ Características (Features)

* **Autenticación Completa:** Sistema de **Registro** y **Login** de usuarios.
* **Seguridad:** Autenticación basada en **Tokens JWT** (JSON Web Tokens).
* **Recuperación de Cuenta:** Flujo completo de "Olvidé mi Contraseña" usando una **API de email real** (SendGrid).
* **Juego de Sudoku:**
    * **Generador Aleatorio:** El backend genera tableros de Sudoku únicos por cada partida.
    * **Modos de Juego:** Elige entre "Modo Libre" o "Desafío con Tiempo" (Fácil, Medio, Difícil).
    * **Temporizador:** Cuenta atrás en tiempo real para los desafíos.
    * **Validación:** El tablero valida en tiempo real (con CSS) si hay números duplicados.
    * **Guardado de Partidas:** Las partidas "En Progreso" se guardan en la base de datos (con `POST /save`).
    * **Sistema de Puntuación:** Las partidas "Completadas" se guardan para el historial.
* **Historial de Partidas:** Página de "Scoreboard" que muestra las partidas completadas y sus mejores tiempos.
* **100% Dockerizado:** Toda la arquitectura (Frontend, Backend, BBDD) se levanta con un solo comando.

---

## 🛠️ Stack Tecnológico

* **Backend:** **Java 17**, **Spring Boot 3** (con Spring Security, Spring Data JPA).
* **Frontend:** **Angular 17+**, TypeScript, Angular Material.
* **Base de Datos:** **PostgreSQL** (corriendo en un contenedor Docker).
* **Contenedorización:** **Docker** y **Docker Compose**.
* **Servicio de Email:** **SendGrid** API.

---

## 🚀 Cómo Ejecutarlo (Demo Local)

Este proyecto está diseñado para levantarse con un solo comando gracias a Docker.

### Pre-requisitos
* [Git](https://git-scm.com/)
* [Docker](https://www.docker.com/products/docker-desktop/)
* [Docker Compose](https://docs.docker.com/compose/install/)

### 1. Clonar el Repositorio
```bash
git clone [https://github.com/monzonmariano/logic-games-fullstack-Angular-Openjdk-17-.git](https://github.com/monzonmariano/logic-games-fullstack-Angular-Openjdk-17-.git)
cd logic-games-fullstack-Angular-Openjdk-17-
