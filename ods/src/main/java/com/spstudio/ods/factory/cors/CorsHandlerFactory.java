package com.spstudio.ods.factory.cors;

import io.vertx.ext.web.handler.CorsHandler;

public abstract class CorsHandlerFactory {

	public abstract CorsHandler createCorsHandler();
}
