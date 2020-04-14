package lt.galdebar.monmonmvc.context.security.jwt;

import io.jsonwebtoken.*;
import lt.galdebar.monmonmvc.context.security.MongoUserDetailsService;
import lt.galdebar.monmonmvc.context.security.exceptions.InvalidJwtAuthenticationException;
import lt.galdebar.monmonmvc.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Handles creation and verification of Authorization tokens
 */
@Component
public class JwtTokenProvider {
    @Value("${security.jwt.token.secret-key:secret}")
    private String secretKey = "MonSecretMon";
    @Value("${security.jwt.token.expire-length:3600000}")
    private long validityInMilliseconds = 3600000; // 1h

    @Autowired
    private MongoUserDetailsService userDetailsService;

    @Autowired
    private UserService userService;

    /**
     * Init. Set encoder
     */
    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    /**
     * Create Authorization token.
     *
     * @param username the username
     * @param roles    the roles
     * @return the string
     */
    public String createToken(String username, List<String> roles) {
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("roles", roles);
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);
        return Jwts.builder()//
                .setClaims(claims)//
                .setIssuedAt(now)//
                .setExpiration(validity)//
                .signWith(SignatureAlgorithm.HS256, secretKey)//
                .compact();
    }

    /**
     * Gets authentication.
     *
     * @param token the token
     * @return the authentication
     */
    Authentication getAuthentication(String token) {
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(getUsername(token));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    private String getUsername(String token) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getSubject();
    }

    /**
     * Parses Authorization header and retrieves the token.
     *
     * @param req the req
     * @return the string
     */
    String resolveToken(HttpServletRequest req) {
        String bearerToken = req.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return getActualToken(bearerToken);
        }
        return null;
    }

    /**
     * Validate token.
     *
     * @param token the token
     * @return the boolean
     * @throws InvalidJwtAuthenticationException the invalid jwt authentication exception
     */
    boolean validateToken(String token) throws InvalidJwtAuthenticationException {
        try {
            Jws<Claims> claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            if (claims.getBody().getExpiration().before(new Date())) {
                return false;
            }
            if(userService.checkIfUserIsPendingDeletion(claims.getBody().getSubject())){
                throw new InvalidJwtAuthenticationException("Expired or invalid JWT token");
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
