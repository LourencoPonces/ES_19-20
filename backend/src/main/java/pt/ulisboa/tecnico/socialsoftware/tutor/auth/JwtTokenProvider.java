package pt.ulisboa.tecnico.socialsoftware.tutor.auth;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository;

import javax.crypto.SecretKey;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Optional;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.USER_NOT_FOUND;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    public static final String COOKIE_NAME = "quiztutorauth";
    public static final int EXPIRATION_TIME = 1000 * 60 * 60 * 24;

    private UserRepository userRepository;

    private SecretKey secretKey;

    private JwtParser jwtParser;

    public JwtTokenProvider(UserRepository userRepository, @Value("${auth.secret}") String secret) {
        this.userRepository = userRepository;
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.jwtParser = Jwts.parserBuilder().setSigningKey(secretKey).build();
    }

    private static SecretKey generateSecret() {
        return Keys.secretKeyFor(SignatureAlgorithm.HS512);
    }

    String generateToken(User user) {
        Claims claims = Jwts.claims().setSubject(String.valueOf(user.getId()));
        claims.put("role", user.getRole());

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    static String getToken(HttpServletRequest req) {
        String authHeader = req.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        } else if (authHeader != null && authHeader.startsWith("AUTH")) {
            return authHeader.substring(4);
        } else if (authHeader != null) {
            return authHeader;
        }

        if (req.getCookies() != null) {
            for (Cookie cookie : req.getCookies()) {
                if (cookie.getName().equals(COOKIE_NAME))
                    return cookie.getValue();
            }
        }

        return "";
    }

    Optional<Integer> getUserId(String token) {
        try {
            return Optional.of(Integer.parseInt(jwtParser.parseClaimsJws(token).getBody().getSubject()));
        } catch (SignatureException ex) {
            logger.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty.");
        }

        return Optional.empty();
    }

    Authentication getAuthentication(String token) {
        return getUserId(token)
                .map(id -> this.userRepository.findById(id)
                        .orElseThrow(() -> new TutorException(USER_NOT_FOUND, id)))
                .map(user -> new UsernamePasswordAuthenticationToken(user, "", user.getAuthorities()))
                .orElse(null);
    }
}
