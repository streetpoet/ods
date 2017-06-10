package com.spstudio.ods.factory.cors;

import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.handler.CorsHandler;

public class OdsApiCorsHandlerFactory extends CorsHandlerFactory {

	@Override
	public CorsHandler createCorsHandler() {
		return CorsHandler.create("http://localhost:8080").allowCredentials(true)
				.allowedHeader("Access-Control-Allow-Method").allowedHeader("Access-Control-Allow-Origin")
				.allowedHeader("Access-Control-Allow-Credentials").allowedHeader("Content-Type")
				.allowedMethod(HttpMethod.GET).allowedMethod(HttpMethod.POST).allowedMethod(HttpMethod.OPTIONS)
				.allowedMethod(HttpMethod.PUT).allowedMethod(HttpMethod.DELETE).maxAgeSeconds(1800);
	}

}
