package com.devcourse.kurlymurly.auth.jwt;

import com.devcourse.kurlymurly.auth.CustomUserDetailService;
import com.devcourse.kurlymurly.common.exception.ErrorCode;
import com.devcourse.kurlymurly.common.exception.KurlyBaseException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.xml.bind.DatatypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

import static com.devcourse.kurlymurly.common.exception.ErrorCode.EXPIRED_JWT_TOKEN;
import static com.devcourse.kurlymurly.common.exception.ErrorCode.NOT_AUTHORIZED_TOKEN;
import static com.devcourse.kurlymurly.common.exception.ErrorCode.NOT_CORRECT_JWT;
import static com.devcourse.kurlymurly.common.exception.ErrorCode.NOT_CORRECT_JWT_SIGN;
import static com.devcourse.kurlymurly.common.exception.ErrorCode.NOT_SUPPORTED_JWT_TOKEN;

@Component
public class JwtProvider {
    private static final Logger log = LoggerFactory.getLogger(JwtProvider.class);
    private static final long EXPIRATION_TIME = 30 * 60 * 1000L;
    private static final String AUTHORITY = "authority";

    private final Key key;
    private final CustomUserDetailService userDetailService;

    public JwtProvider(
            @Value("${secret-key}") String secretKey,
            CustomUserDetailService userDetailService
    ) {
        byte[] secretByteKey = DatatypeConverter.parseBase64Binary(secretKey);
        this.key = Keys.hmacShaKeyFor(secretByteKey);
        this.userDetailService = userDetailService;
    }

    public String createToken(Authentication authentication) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .setSubject(authentication.getName())
                .claim(AUTHORITY, authorities)
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Authentication getAuthentication(String accessToken) {
        Claims claims = parseClaims(accessToken);
        String username = claims.getSubject();
        Collection<? extends GrantedAuthority> authorities = getAuthority(claims);

        UserDetails user = userDetailService.loadUserByUsername(username);
        return new UsernamePasswordAuthenticationToken(user, null, authorities);
    }

    private Collection<? extends GrantedAuthority> getAuthority(Claims claims) {
        try {
            String[] authorities = claims.get(AUTHORITY).toString().split(",");
            return Arrays.stream(authorities)
                    .map(SimpleGrantedAuthority::new)
                    .toList();
        } catch (KurlyBaseException e) {
            throw new KurlyBaseException(NOT_AUTHORIZED_TOKEN);
        }
    }

    public boolean isValidToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.warn("JWT Exception Occurs : {}", NOT_CORRECT_JWT_SIGN);
        } catch (ExpiredJwtException e) {
            log.warn("JWT Exception Occurs : {}", EXPIRED_JWT_TOKEN);
        } catch (UnsupportedJwtException e) {
            log.warn("JWT Exception Occurs : {}", NOT_SUPPORTED_JWT_TOKEN);
        } catch (IllegalArgumentException e) {
            log.warn("JWT Exception Occurs : {}", NOT_CORRECT_JWT);
        }

        throw new KurlyBaseException(ErrorCode.CHECK_TOKEN_ERROR);
    }

    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }
}

