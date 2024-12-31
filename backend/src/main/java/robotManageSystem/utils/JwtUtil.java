package robotManageSystem.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.huawei.innovation.rdm.intelligentrobotengineering.dto.entity.UserViewDTO;

import java.util.Date;

@Component
public class JwtUtil {
    @Value("${jwt.secret:your-secret-key}")
    private String secret;

    @Value("${jwt.expiration:604800}")  // 7天
    private long expiration;

    public String generateToken(UserViewDTO user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration * 1000);

        return Jwts.builder()
            .setSubject(user.getId().toString())
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(SignatureAlgorithm.HS512, secret)
            .compact();
    }

    public String getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
            return claims.getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    public void clearToken(String token) {
        // 清除token
        JwtParser parser = Jwts.parser().setSigningKey(secret);
        JwtBuilder builder = Jwts.builder().signWith(SignatureAlgorithm.HS512, secret);
        parser.parse(token);
        builder.compact();
    }

    public String validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();

            return claims.getSubject();
        } catch (Exception e) {
            return null;
        }
    }
}