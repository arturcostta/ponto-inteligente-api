package com.kazale.pontointeligente.api.services.impl;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.kazale.pontointeligente.api.entities.Lancamento;
import com.kazale.pontointeligente.api.repositories.LancamentoRepository;
import com.kazale.pontointeligente.api.services.LancamentoService;
@Service
public class LancamentoServiceImpl implements LancamentoService {
	
	private static final Logger log = LoggerFactory.getLogger(FuncionarioServiceImpl.class);
	
	@Autowired
	private LancamentoRepository lancRepository;
	
	@Override
	public Page<Lancamento> buscarPorFuncionarioId(Long funcionarioId, PageRequest request) {
		log.info("Buscando lançamentos para o funcionário id {}",funcionarioId);
		return this.lancRepository.findByFuncionarioId(funcionarioId, request);
	}

	@Override
	@Cacheable("lancamentoPorId")
	public Optional<Lancamento> buscarPorId(Long id) {
		log.info("Buscando lançamentos por id {}",id);
		return Optional.ofNullable( this.lancRepository.findOne(id) );
	}
	
	

	@Override
	@CachePut("lancamentoPorId")
	public Lancamento persistir(Lancamento lancamento) {
		log.info("Persistindo um lançamento {}",lancamento);
		return this.lancRepository.save(lancamento);
	}

	public void remover(Long id) {
		log.info("Removendo um lançamento {}",id);
		this.lancRepository.delete(id);
	}

}
