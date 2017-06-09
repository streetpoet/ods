package com.spstudio.ods;

import io.vertx.core.Verticle;
import io.vertx.core.Vertx;

public class Client {

	public static void main(String[] args) {
		Vertx vertx = Vertx.vertx();
		
		Verticle verticle = new HttpServerVerticle();
		vertx.deployVerticle(verticle, r -> {
			if (r.succeeded()) {
				System.out.println(r.result());
			}else {
				System.out.println(r.cause());
			}
		});
		
		Verticle restApiVerticle = new RestApiVerticle();
		vertx.deployVerticle(restApiVerticle, r -> {
			if (r.succeeded()) {
				System.out.println(r.result());
			}else {
				System.out.println(r.cause());
			}
		});
	}

}
