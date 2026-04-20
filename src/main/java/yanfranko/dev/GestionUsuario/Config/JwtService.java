package yanfranko.dev.GestionUsuario.Config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.JwtException;
import yanfranko.dev.GestionUsuario.Models.Usuario;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    private final String SECRET_KEY;

    private final long EXPIRATION_TIME = 1000L * 60 * 60 * 24; // 24 horas

    public JwtService(@Value("${jwt.secret:}") String secret) {
        this.SECRET_KEY = secret;
    }

    public String generarToken(Usuario usuario) {

    if (SECRET_KEY == null || SECRET_KEY.isBlank()) {
        throw new IllegalStateException("JWT Secret not configured");
    }

    return Jwts.builder()
            .subject(usuario.getEmail()) 
            .issuedAt(new Date())       
            .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
            .signWith(getSigningKey())   
            .compact();
}

    public String extractUsername(String token) {
        return extraerClaim(token, Claims::getSubject);
    }

    // valida el token
    public boolean esTokenValido(String token, String emailUsuario) {
        final String emailToken = extractUsername(token);
        return (emailToken != null && emailToken.equals(emailUsuario) && !estaExpirado(token));
    }

    private boolean estaExpirado(String token) {
        Date exp = extraerClaim(token, Claims::getExpiration);
        if (exp == null)
            return true;
        return exp.before(new Date());
    }

    public <T> T extraerClaim(String token, Function<Claims, T> claimsResolver) {
    try {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())   // ✔️ reemplaza setSigningKey
                .build()
                .parseSignedClaims(token)      // ✔️ reemplaza parseClaimsJws
                .getPayload();                 // ✔️ reemplaza getBody()

        return claimsResolver.apply(claims);

    } catch (JwtException | IllegalArgumentException e) {
        return null;
    }
}

    private SecretKey getSigningKey() {
        if (SECRET_KEY == null || SECRET_KEY.isBlank()) {
            throw new IllegalStateException("JWT secret not configured");
        }
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }
}
