package com.kazale.pontointeligente.api.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.kazale.pontointeligente.api.entities.Funcionario;
import com.kazale.pontointeligente.api.enums.PerfilEnum;

public class JwtUserFactory {

	private JwtUserFactory() {
		
	}
	
	/**
	 * Converte e gera um JwtUser com base nos dados do funcionário
	 * @param usuario
	 * @return
	 */
	public static JwtUser create(Funcionario funcionario) {
		return new JwtUser(funcionario.getId(), funcionario.getEmail(), funcionario.getSenha(), mapToGranthedAuthorities(funcionario.getPerfil()));
	}

	/**
	 * Converte o perfil do usuário para o formato utilizado pelo Spring Security
	 * @param perfil
	 * @return
	 */
	private static List<GrantedAuthority> mapToGranthedAuthorities(PerfilEnum perfil) {
		List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		authorities.add(new SimpleGrantedAuthority(perfil.toString()));
		return authorities;
	}
}
