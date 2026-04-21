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
import java.util.List;
import java.util.function.Function;

@Service
public class JwtService {

    private final String SECRET_KEY;

    private final long ACCESS_EXPIRATION_TIME = 1000L * 60 * 15; // 15 minutos
    private final long REFRESH_EXPIRATION_TIME = 1000L * 60 * 60 * 24 * 7; // 7 días

    public JwtService(@Value("${jwt.secret:}") String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT secret must be configured via JWT_SECRET environment variable");
        }
        this.SECRET_KEY = secret;
    }

    public String generarAccessToken(Usuario usuario) {
        return Jwts.builder()
                .subject(usuario.getEmail())
                .claim("roles", List.of(usuario.getRole().getERole().name()))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ACCESS_EXPIRATION_TIME))
                .signWith(getSigningKey())
                .compact();
    }

    public String generarRefreshToken(Usuario usuario) {
        return Jwts.builder()
                .subject(usuario.getEmail())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + REFRESH_EXPIRATION_TIME))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractUsername(String token) {
        return extraerClaim(token, Claims::getSubject);
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        return extraerClaim(token, claims -> (List<String>) claims.get("roles"));
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
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claimsResolver.apply(claims);

        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }
}
