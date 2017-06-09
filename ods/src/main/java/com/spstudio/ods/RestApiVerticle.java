package com.spstudio.ods;

import java.util.Collections;

import com.spstudio.ods.config.DBConfigFactory;
import com.spstudio.ods.config.MySQLDBConfigFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;

public class RestApiVerticle extends AbstractVerticle {

	@Override
	public void start(Future<Void> startFuture) throws Exception {

		HttpServerOptions options = new HttpServerOptions();
		options.setLogActivity(true);
		HttpServer server = vertx.createHttpServer();

		Router router = Router.router(vertx);

		DBConfigFactory dbConfigFactory = new MySQLDBConfigFactory();
		JDBCClient client = JDBCClient.createShared(vertx, dbConfigFactory.createDatabaseConfig());

		router.route().handler(CorsHandler.create("*"));
		router.route().handler(BodyHandler.create());

		router.post("/api/labels").handler(rc -> {
			client.getConnection(res -> {
				if (res.succeeded()) {
					JsonObject labelEntity = rc.getBodyAsJson();
					res.result().updateWithParams("INSERT INTO LABEL(label_name) VALUES (?)",
							new JsonArray().add(labelEntity.getString("labelName")), r -> {
								if (r.succeeded()) {
									rc.response().end(Json.encode(Collections.singletonMap("result", "ok")));
								} else {
									rc.response()
										.setStatusCode(500)
										.setStatusMessage(r.cause().getLocalizedMessage())
										.end();
								}
							});
				} else {
					rc.response().setStatusCode(500).end();
				}

			});
		});

		router.get("/api/labels").blockingHandler(rc -> {
			client.getConnection(res -> {
				if (res.succeeded()) {
					res.result().query("SELECT id, label_name FROM LABEL", r -> {
						if (r.succeeded()) {
							rc.response().setChunked(true);
							rc.response().write(Json.encodePrettily(r.result().getRows()));
							rc.response().end();
						}
					});
				}
			});
		});

		server.requestHandler(router::accept).listen(8090, r -> {
			if (r.succeeded()) {
				System.out.println("Server is now listening at 8090!");
			} else {
				System.out.println("Failed to bind!");
			}
		});
		
		startFuture.complete();
	}

}
