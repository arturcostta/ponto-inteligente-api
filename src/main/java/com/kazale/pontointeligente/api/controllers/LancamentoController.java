package com.kazale.pontointeligente.api.controllers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Optional;

import javax.validation.Valid;

import org.apache.commons.lang3.EnumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kazale.pontointeligente.api.dtos.FuncionarioDto;
import com.kazale.pontointeligente.api.dtos.LancamentoDto;
import com.kazale.pontointeligente.api.entities.Funcionario;
import com.kazale.pontointeligente.api.entities.Lancamento;
import com.kazale.pontointeligente.api.enums.TipoEnum;
import com.kazale.pontointeligente.api.response.Response;
import com.kazale.pontointeligente.api.services.FuncionarioService;
import com.kazale.pontointeligente.api.services.LancamentoService;

@RestController
@RequestMapping("/api/lancamentos")
@CrossOrigin(origins = "*")
public class LancamentoController {
	
	private static final Logger log = LoggerFactory.getLogger(LancamentoController.class);
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	@Autowired
	private LancamentoService lancamentoService;
	
	@Autowired
	private FuncionarioService funcionarioService;
	
	@Value("${paginacao.qtd_por_pagina}")
	private int qtdPorPagina;
	
	public LancamentoController() {
		
	}
	/**
	 * Retorna os lançamentos de um funcionário
	 * @param funcionarioId
	 * @param pag
	 * @param ord
	 * @param dir
	 * @return
	 */
	@GetMapping(value="/funcionario/{funcionarioId}")
	public ResponseEntity<Response<Page<LancamentoDto>>> listarPorFuncionarioId(@PathVariable("funcionarioId") Long funcionarioId,
			@RequestParam(value="pag", defaultValue="0") int pag,
			@RequestParam(value="ord", defaultValue="id") String ord,
			@RequestParam(value="dir", defaultValue="DESC") String dir){
		log.info("Buscando lançamentos por id do funcinário {}, página {}",funcionarioId, pag);
		
		Response<Page<LancamentoDto>> response = new Response<Page<LancamentoDto>>();
		
		PageRequest	pageRequest = new PageRequest(pag, this.qtdPorPagina, Direction.valueOf(dir), ord);
		Page<Lancamento> lancamentos = this.lancamentoService.buscarPorFuncionarioId(funcionarioId, pageRequest);
		Page<LancamentoDto> lancamentoDto = lancamentos.map(lancamento -> this.convertLancamentoDto(lancamento));
		
		response.setData(lancamentoDto);
		
		return ResponseEntity.ok(response);
		
	}
	
	@GetMapping(value="/{id}")
	public ResponseEntity<Response<LancamentoDto>> listarPorId(@PathVariable("id") Long id){
		log.info("Buscando lançamento por ID {}",id);
		Response<LancamentoDto> response = new Response<LancamentoDto>();
		Optional<Lancamento> lancamento = this.lancamentoService.buscarPorId(id);
		
		if(!lancamento.isPresent()) {
			log.info("Lançamento não encontrado para o ID {}", id);
			response.getErrors().add("Lançamento não encontrado para o id {}"+id);
			return ResponseEntity.badRequest().body(response);
		}
		response.setData(this.convertLancamentoDto(lancamento.get()));
		return ResponseEntity.ok(response);
	}
	
	/**
	 * Adicionar novo lançamento
	 * @param lancamentoDto
	 * @param result
	 * @return
	 * @throws ParseException
	 */
	@PostMapping
	public ResponseEntity<Response<LancamentoDto>> adicionar(@Valid @RequestBody LancamentoDto lancamentoDto, BindingResult result) throws ParseException{
		log.info("Adicionando lancamento: {}",lancamentoDto.toString());
		Response<LancamentoDto> response = new Response<LancamentoDto>();
		validarFuncionario(lancamentoDto,result);
		Lancamento lancamento = this.converterDtoParaLancamento(lancamentoDto,result);
		
		if(result.hasErrors()) {
			log.error("Erro validando lancamento {}",result.getAllErrors());
			result.getAllErrors().forEach(error -> response.getErrors().add(error.getDefaultMessage()));
			return ResponseEntity.badRequest().body(response);
		}
		
		lancamento = this.lancamentoService.persistir(lancamento);
		response.setData(this.convertLancamentoDto(lancamento));
		return ResponseEntity.ok(response);
	}
	
	/**
	 * Atualiza os dados de um lançamento
	 * @param id
	 * @param lancamentoDto
	 * @param result
	 * @return
	 * @throws ParseException
	 */
	@PutMapping(value="/{id}")
	public ResponseEntity<Response<LancamentoDto>> atualizar(@PathVariable("id") Long id,
			@Valid @RequestBody LancamentoDto lancamentoDto, BindingResult result) throws ParseException{
		log.info("Atualizando lançamento: {}",lancamentoDto.toString());
		Response<LancamentoDto> response = new Response<LancamentoDto>();	
		validarFuncionario(lancamentoDto,result);
		lancamentoDto.setId(Optional.of(id));
		Lancamento lancamento = this.converterDtoParaLancamento(lancamentoDto, result);
		
		if(result.hasErrors()) {
			log.error("Erro validando lançamento {}", result.getAllErrors());
			result.getAllErrors().forEach(error -> response.getErrors().add(error.getDefaultMessage()));
			return ResponseEntity.badRequest().body(response);
		}
		lancamento = this.lancamentoService.persistir(lancamento);
		response.setData(this.convertLancamentoDto(lancamento));
		return ResponseEntity.ok(response);
	}
/**
 * Converte LancamentoDto para um lancamento
 * @param lancamentoDto
 * @param result
 * @return
 * @throws ParseException
 */
	private Lancamento converterDtoParaLancamento(LancamentoDto lancamentoDto, BindingResult result) throws ParseException{
		Lancamento lancamento = new Lancamento();
		if(lancamentoDto.getId().isPresent()) {
			Optional<Lancamento> lanc = this.lancamentoService.buscarPorId(lancamentoDto.getId().get());
			if(lanc.isPresent()) {
				lancamento = lanc.get();
			}else {
				result.addError(new ObjectError("lancamento", "Lançamento não encontrado."));
			}
		}else {
			lancamento.setFuncionario(new Funcionario());
			lancamento.getFuncionario().setId(lancamentoDto.getFuncionarioId());
		}
		lancamento.setDescricao(lancamentoDto.getDescricao());
		lancamento.setLocalizacao(lancamentoDto.getLocalizacao());
		lancamento.setData(this.dateFormat.parse(lancamentoDto.getData()));
		
		if(EnumUtils.isValidEnum(TipoEnum.class, lancamentoDto.getTipo())) {
			lancamento.setTipo(TipoEnum.valueOf(lancamentoDto.getTipo()));
		}else {
			result.addError(new ObjectError("tipo", "Tipo inválido"));
		}
		
		return lancamento;
	}
	
	@DeleteMapping(value="/{id}")
	@PreAuthorize("hasAnyRole('ADMIN')")
	public ResponseEntity<Response<String>> remover(@PathVariable("id") Long id){
		log.info("Removendo lançamento {}",id);
		Response<String> response = new Response<String>();
		Optional<Lancamento> lancamento = this.lancamentoService.buscarPorId(id);
		if(!lancamento.isPresent()) {
			log.info("Erro ao remover devido ao lançamento id:{} ser inválido.",id);
			response.getErrors().add("Erro ao remover lançamento. Registro não encontrado para o Id "+id);
			return ResponseEntity.badRequest().body(response);
		}
		this.lancamentoService.remover(id);
		return ResponseEntity.ok(response);
	}
	
	/**
	 * verifica se o funcionário existe
	 * @param lancamentoDto
	 * @param result
	 */
	private void validarFuncionario(LancamentoDto lancamentoDto, BindingResult result) {
		if(lancamentoDto.getFuncionarioId() == null) {
			result.addError(new ObjectError("funcionario", "Funcionario não informado"));
			return;
		}
		log.info("Validando funcionario {}",lancamentoDto.getFuncionarioId());
		Optional<Funcionario> funcionario = this.funcionarioService.buscarPorId(lancamentoDto.getFuncionarioId());
		if(!funcionario.isPresent()) {
			result.addError(new ObjectError("funcionario", "Funcionario nao encontrado. ID inexistente."));
		}
	}
	/**
	 * Converte uma entidade lancamento para dto
	 * @param lancamento
	 * @return
	 */
	private LancamentoDto convertLancamentoDto(Lancamento lancamento) {
		LancamentoDto lancamentoDto = new LancamentoDto();
		lancamentoDto.setId(Optional.of(lancamento.getId()));
		lancamentoDto.setData(this.dateFormat.format(lancamento.getData()));
		lancamentoDto.setTipo(lancamento.getTipo().toString());
		lancamentoDto.setDescricao(lancamento.getDescricao());
		lancamentoDto.setLocalizacao(lancamento.getLocalizacao());
		lancamentoDto.setFuncionarioId(lancamento.getFuncionario().getId());
		return lancamentoDto;
	}
}
