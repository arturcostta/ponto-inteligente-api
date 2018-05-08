package com.kazale.pontointeligente.api.services;

import java.util.Optional;

import com.kazale.pontointeligente.api.security.entities.Usuario;

public interface UsuarioService {
	
	/**
	 * Busca e retorna um usuario dado um email
	 * @param email
	 * @return
	 */
	Optional<Usuario> buscarPorEmail(String email);
}
