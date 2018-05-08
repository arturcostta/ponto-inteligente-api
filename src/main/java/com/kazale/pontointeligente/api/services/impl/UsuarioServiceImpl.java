package com.kazale.pontointeligente.api.services.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import com.kazale.pontointeligente.api.repositories.UsuarioRepository;
import com.kazale.pontointeligente.api.security.entities.Usuario;
import com.kazale.pontointeligente.api.services.UsuarioService;

public class UsuarioServiceImpl implements UsuarioService {

	@Autowired
	private UsuarioRepository usuarioRepository;
	
	@Override
	public Optional<Usuario> buscarPorEmail(String email) {
		
		return Optional.ofNullable(this.usuarioRepository.findByEmail(email));
	}

}
