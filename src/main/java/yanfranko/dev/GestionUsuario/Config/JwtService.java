package yanfranko.dev.GestionUsuario.Config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import yanfranko.dev.GestionUsuario.Models.Usuario;

import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    private final String SECRET_KEY = "MKFGNR$#%$/&%&()=IGnn%uiBYUyvreiog89t56*/-+.IUVTYv6759u9J&/><<<<|°165254&%YTGG";
    
    private final long EXPIRATION_TIME = 1000 * 60 * 60 * 24; // 24 horas

    public String generarToken(Usuario usuario) {
        return Jwts.builder()
                .setSubject(usuario.getEmail())
                .claim("roles", usuario.getRole().getERole().name()) // Agregamos el rol como claim
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) 
                .compact();
    }

    public String extractUsername(String token) {
        return extraerClaim(token, Claims::getSubject);
    }

    //valida el token
    public boolean esTokenValido(String token, String emailUsuario) {
        final String emailToken = extractUsername(token);
        return (emailToken.equals(emailUsuario) && !estaExpirado(token));
    }

    private boolean estaExpirado(String token) {
        return extraerClaim(token, Claims::getExpiration).before(new Date());
    }

    public <T> T extraerClaim(String token, Function<Claims, T> claimsResolver ) {
        final Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claimsResolver.apply(claims);
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }
}
