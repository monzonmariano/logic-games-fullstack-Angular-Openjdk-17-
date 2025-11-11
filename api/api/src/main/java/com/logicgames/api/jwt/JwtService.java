package com.logicgames.api.jwt;

import com.logicgames.api.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    // 1. La "llave secreta" para firmar los carnets.
    // ¡Debería estar en un archivo de propiedades, pero esto funciona por ahora!
    // Es una llave de 256 bits codificada en Base64.
    @Value("${app.jwt.secret-key}")
    private String SECRET_KEY;

    // --- 1. ¡NUESTRA CONSTANTE DE TIEMPO! ---
    // 7 Días en milisegundos: 1000ms * 60s * 60m * 24h * 7d
    // Usamos 'L' al final para decirle a Java que es un número 'Long'
    private static final long EXPIRATION_TIME_MS = 1000L * 60 * 60 * 24 * 7;

    // 2. Método principal
    public String generateToken(User user) {
        return generateToken(new HashMap<>(), user);
    }

    public String generateToken(Map<String, Object> extraClaims, User user) {
        return Jwts.builder()
                .setClaims(extraClaims) // Información extra (ej. roles)
                .setSubject(user.getEmail()) // El "dueño" del carnet (nuestro "username")
                .setIssuedAt(new Date(System.currentTimeMillis())) // Cuándo se emitió
                // Ahora usa nuestra constante de 7 días
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME_MS))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256) // La firma con nuestra llave
                .compact(); // Constrúyelo
    }

    // 3. Método para "leer" el carnet: Extrae el email (username)
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // 4. Métodos de ayuda para "leer" el carnet
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 5. Método de ayuda para "traducir" la llave secreta
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Nuevo método: Comprueba si un token es válido
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        // Comprueba si el email del token coincide con el de la BBDD
        // Y si el token NO ha caducado
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    // Nuevo método: Comprueba si el token ha caducado
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Nuevo método: Extrae la fecha de expiración
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

}
