package com.kazale.pontointeligente.api.controllers;

import java.security.NoSuchAlgorithmException;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kazale.pontointeligente.api.dtos.CadastroPJDto;
import com.kazale.pontointeligente.api.entities.Empresa;
import com.kazale.pontointeligente.api.entities.Funcionario;
import com.kazale.pontointeligente.api.response.Response;
import com.kazale.pontointeligente.api.services.EmpresaService;
import com.kazale.pontointeligente.api.services.FuncionarioService;

@RestController
@RequestMapping("/api/cadastrar-pj")
@CrossOrigin(origins = "*")
public class CadastroPJController {

	private static final Logger log = LoggerFactory.getLogger(CadastroPJController.class);

	@Autowired
	private EmpresaService empresaService;
	@Autowired
	private FuncionarioService funcionarioService;

	public CadastroPJController() {

	}

	/**
	 * Cadastra uma pessoa juridica no sistema
	 * 
	 * @param cadastroPJDto
	 * @param result
	 * @return
	 */
	@PostMapping
	public ResponseEntity<Response<CadastroPJDto>> cadastrar(@Valid @RequestBody CadastroPJDto cadastroPJDto,
			BindingResult result) throws NoSuchAlgorithmException {
		log.info("Cadastrando PJ: {}", cadastroPJDto.toString());
		Response<CadastroPJDto> response = new Response<CadastroPJDto>();

		validarDadosExistentes(cadastroPJDto, result);
		Empresa empresa = this.convertDtoParaEmpresa(cadastroPJDto);
		Funcionario funcionario = this.convertDtoParaFuncionario(cadastroPJDto);

		if (result.hasErrors()) {
			log.error("Erro validando dados de cadastro PJ {}", result.getAllErrors());
			result.getAllErrors().forEach(error -> response.getErrors().add(error.getDefaultMessage()));
			return ResponseEntity.badRequest().body(response);
		}

		this.empresaService.persistir(empresa);
		funcionario.setEmpresa(empresa);
		this.funcionarioService.persistir(funcionario);

		return null;
	}

	/**
	 * Converte os dados do dto para funcionário
	 * @param cadastroPJDto
	 * @return
	 */
	private Funcionario convertDtoParaFuncionario(@Valid CadastroPJDto cadastroPJDto) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Converte os dados do dto para empresa
	 * @param cadastroPJDto
	 * @return
	 */
	private Empresa convertDtoParaEmpresa(@Valid CadastroPJDto cadastroPJDto) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Verifica se empresa e funcionario ja existem na base
	 * @param cadastroPJDto
	 * @param result
	 */
	private void validarDadosExistentes(@Valid CadastroPJDto cadastroPJDto, BindingResult result) {
		this.empresaService.buscarPorCnpj(cadastroPJDto.getCnpj())
				.ifPresent(emp -> result.addError(new ObjectError("empresa", "Empresa já existente.")));
		this.funcionarioService.buscarPorCpf(cadastroPJDto.getCpf())
				.ifPresent(func -> result.addError(new ObjectError("funcionario", "Cpf já existente.")));
		this.funcionarioService.buscarPorEmail(cadastroPJDto.getEmail())
				.ifPresent(func -> result.addError(new ObjectError("funcionario", "Email já existente.")));
	}
}
