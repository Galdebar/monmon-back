package lt.galdebar.monmonapi.app.context.security.jwt;

import io.jsonwebtoken.*;
import lt.galdebar.monmonapi.app.context.security.ShoppingListDetailsService;
import lt.galdebar.monmonapi.app.context.security.jwt.exceptions.InvalidJwtAuthenticationException;
import lt.galdebar.monmonapi.app.services.blacklistedtokens.BlacklistedTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@Component
public class JwtTokenProvider {
    @Value("${security.jwt.token.secret-key:secret}")
    private String secretKey = "MonSecretMon";
    @Value("${security.jwt.token.expire-length:3600000}")
    private long validityInMilliseconds = 3600000; // 1h

    @Autowired
    private ShoppingListDetailsService listDetailsService;

    @Autowired
    private BlacklistedTokenService tokenService;

    @PostConstruct
    protected void init(){
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    public String createToken(String listName, List<String> roles){
        Claims claims = Jwts.claims().setSubject(listName);
        claims.put("roles", roles);
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS256,secretKey)
                .compact();
        return token;
    }

    Authentication getAuthentication(String token){
        UserDetails userDetails = this.listDetailsService.loadUserByUsername(getListName(token));
        return new UsernamePasswordAuthenticationToken(userDetails,"",userDetails.getAuthorities());
    }

    private String getListName(String token) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getSubject();
    }

    public String resolveToken(HttpServletRequest req) {
        String bearerToken = req.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return getActualToken(bearerToken);
        }
        return null;
    }

    boolean validateToken(String token) throws InvalidJwtAuthenticationException {
        try {
            if(tokenService.tokenExists(token)){
                throw new InvalidJwtAuthenticationException("Expired or invalid JWT token");
            }

            Jws<Claims> claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            if (claims.getBody().getExpiration().before(new Date())) {
                return false;
            }
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidJwtAuthenticationException("Expired or invalid JWT token");
        }
    }

    private String getActualToken(String fullString){
        if (fullString != null && fullString.startsWith("Bearer ")) {
            return fullString.substring(7);
        } else return "";
    }
}
