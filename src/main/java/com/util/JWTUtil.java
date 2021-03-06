package com.util;


import com.entity.User;
import com.entity.UserRole;
import com.exception.UserException;
import com.model.UserModel;
import io.jsonwebtoken.*;
import io.jsonwebtoken.impl.crypto.MacProvider;
import io.jsonwebtoken.lang.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.util.Date;
import java.util.Properties;

@Component
public class JWTUtil {
    private Resource resource = new ClassPathResource("/application.properties");
    private Properties props = PropertiesLoaderUtils.loadProperties(resource);
    private final byte[] key = props.getProperty("JWTKey").getBytes("UTF-8");

    @Autowired
    private UserModel userModel;

    @Autowired
    public JWTUtil() throws IOException {
    }

    /**
     * Takes in a User object and generates a JWT-token holding relevant user information
     *
     * @param user user object
     * @return generated JWT-token
     * @throws UnsupportedEncodingException
     */
    public String generateJWT(User user) {
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        return Jwts.builder()
                .setSubject("user")
                .setIssuedAt(now)
                .claim("distortion", nowMillis)
                .claim("userId", user.getId())
                .claim("role", user.getRole())
                .signWith(SignatureAlgorithm.HS512, key)
                .compact();

    }

    /**
     * Takes in a JWT-token and attempts to parses it using our key
     *
     * @param token JWT-token sent by the client
     * @return claims object that holds the parsed information
     * @throws UnsupportedEncodingException if the parsing fails (token is invalid)
     */
    public User parseJWT(String token) throws UnsupportedEncodingException, UserException {

        Jws<Claims> claims = Jwts.parser()
                .requireSubject("user")
                .setSigningKey(key)
                .parseClaimsJws(token);

        int userId = claims.getBody().get("userId", Integer.class);
        System.out.println(userId);
        User user = userModel.getById(userId);

        return user;
    }

}
