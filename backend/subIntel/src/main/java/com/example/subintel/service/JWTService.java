package com.example.subintel.service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JWTService {
	@Value("${jwt.secret.key}")
	private String secretKeyString;

	public String generateToken(String username) {
		Map<String, Object> claims = new HashMap<>();

		return Jwts.builder().setClaims(claims).setSubject(username).setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
				.signWith(getSigningKey(), SignatureAlgorithm.HS256).compact();
	}

	private Key getSigningKey() {
		byte[] keyBytes = Decoders.BASE64.decode(secretKeyString);
		return Keys.hmacShaKeyFor(keyBytes);
	}
}
