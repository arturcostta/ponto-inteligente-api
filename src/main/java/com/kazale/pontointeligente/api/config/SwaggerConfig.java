package com.kazale.pontointeligente.api.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.kazale.pontointeligente.api.utils.JwtTokenUtil;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.ApiKeyVehicle;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@Profile("dev")
@EnableSwagger2
public class SwaggerConfig {

	@Autowired
	private JwtTokenUtil jwtTokenUtil;
	
	@Autowired
	private UserDetailsService userDetailsService;
	
	@Bean
	public Docket api() {
		return new Docket(DocumentationType.SWAGGER_2).select().apis(RequestHandlerSelectors.basePackage("com.kazale.pontointeligente.api.controllers"))
				.paths(PathSelectors.any()).build()
				.apiInfo(apinfo());
	}

	private ApiInfo apinfo() {
		// TODO Auto-generated method stub
		return new ApiInfoBuilder().title("Ponto inteligente API").version("1.0").build();
	}
	
	@Bean
	public SecurityConfiguration security() {
		String token;
		try{
			UserDetails userDetails = this.userDetailsService.loadUserByUsername("admin@kazale.com");
			token = this.jwtTokenUtil.obterToken(userDetails);
		}catch(Exception ex) {
			token = "";
		}
		return new SecurityConfiguration(null, null, null, null, "Bearer "+token, ApiKeyVehicle.HEADER, "Authorization", ",");
	}
}
