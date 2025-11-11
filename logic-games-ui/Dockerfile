# --- Fase 1: Construcción (El "Taller") ---
# Usa una imagen de Node.js
FROM node:20-alpine AS builder

WORKDIR /app

# Copia el package.json y lo instala
COPY package*.json ./
RUN npm install

# Copia el resto del código de Angular
COPY . .

# ¡Construye la aplicación para producción!
RUN npm run build

# --- Fase 2: Producción (El "Servidor Web") ---
# Usa una imagen de Nginx (un servidor web ligero)
FROM nginx:1.25-alpine

# Copia los archivos de la app (construidos en la Fase 1) 
# a la carpeta donde Nginx sirve el HTML
COPY --from=builder /app/dist/logic-games-ui/browser /usr/share/nginx/html

# ¡IMPORTANTE! Copia nuestra configuración personalizada
# (El archivo que crearemos en el siguiente paso)
COPY nginx.conf /etc/nginx/nginx.conf

# Expone el puerto 80 (el puerto de Nginx)
EXPOSE 80
