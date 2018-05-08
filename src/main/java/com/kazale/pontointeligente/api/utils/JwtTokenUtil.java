package com.kazale.pontointeligente.api.utils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtTokenUtil {

	static final String CLAIM_KEY_USERNAME = "sub";
	static final String CLAIM_KEY_ROLE = "role";
	static final String CLAIM_KEY_CREATED = "created";
	
	@Value("${jwt.secret}")
	private String secret;
	
	@Value("${jwt.expiration}")
	private Long expiration;
	
	/**
	 * Obtem username contido no token JWT
	 * @param token
	 * @return
	 */
	public String getUserNameFromToken(String token) {
		String userName;
		try {
			Claims claims = getClaimsFromToken(token);
			userName = claims.getSubject();
		}catch(Exception ex) {
			userName = null;
		}
		return userName;
	}
	
	/**
	 * Retorna a data de expiração de um token JWT
	 * @param token
	 * @return
	 */
	public Date getExpirationDatefromToken(String token) {
		Date expiration;
		try {
			Claims claims = getClaimsFromToken(token);
			expiration = claims.getExpiration();
		}catch(Exception ex) {
			expiration = null;
		}
		return expiration;
	}
	
	/**
	 * Cria um novo token
	 * @param token
	 * @return
	 */
	public String refreshToken(String token) {
		String refreshedToken;
		try {
			Claims claims = getClaimsFromToken(token);
			claims.put(CLAIM_KEY_CREATED, new Date());
			refreshedToken = gerarToken(claims);
		}catch(Exception ex) {
			refreshedToken = null;
		}
		return refreshedToken;
	}
	
	/**
	 * Verifica se um token é valido
	 * @param token
	 * @return
	 */
	public boolean tokenValido(String token) {
		return !tokenExpirado(token);
	}
	
	/**
	 * Retorna um novo token com base nos dados do usuário
	 * @param userDetails
	 * @return
	 */
	public String obterToken(UserDetails userDetails) {
		Map<String, Object> claims = new HashMap<>();
		claims.put(CLAIM_KEY_USERNAME, userDetails.getUsername());
		userDetails.getAuthorities().forEach(authority -> claims.put(CLAIM_KEY_ROLE, authority.getAuthority()));
		claims.put(CLAIM_KEY_CREATED, new Date());
		return gerarToken(claims);
	}

	/**
	 * Verifica se um token JWT está expirado
	 * @param token
	 * @return
	 */
	private boolean tokenExpirado(String token) {
		Date dataExpiracao = this.getExpirationDatefromToken(token);
		if(dataExpiracao == null) {
			return false;
		}
		return dataExpiracao.before(new Date());
	}

	/**
	 * Gera um novo token contendo os dados(claims) fornecidos
	 * @param claims
	 * @return
	 */
	private String gerarToken(Map<String,Object> claims) {
		return Jwts.builder().setClaims(claims).setExpiration(gerarDataExpiração()).signWith(SignatureAlgorithm.HS512, secret).compact();
	}

	/**
	 * Realiza o parse do  token JWT para extrair
	 * as informações contidas no corpo dele
	 * @param token
	 * @return
	 */
	private Claims getClaimsFromToken(String token) {
		Claims claims;
		try {
			claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
		}catch(Exception ex) {
			claims = null;
		}
		return claims;
	}
	
	/**
	 * Retorna a data de expiração com base na data atual
	 * @return
	 */
	private Date gerarDataExpiração() {
		return new Date(System.currentTimeMillis()+expiration*1000);
	}
}
