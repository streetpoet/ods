package com.spstudio.ods;

import com.spstudio.ods.config.DBConfigFactory;
import com.spstudio.ods.config.MySQLDBConfigFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
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

		router.route().handler(CorsHandler.create("http://localhost:8080")
				.allowCredentials(true)
				.allowedHeader("Access-Control-Allow-Method")
				.allowedHeader("Access-Control-Allow-Origin")
				.allowedHeader("Access-Control-Allow-Credentials")
				.allowedHeader("Content-Type")
				.allowedMethod(HttpMethod.GET)
				.allowedMethod(HttpMethod.POST)
				.allowedMethod(HttpMethod.OPTIONS)
				.allowedMethod(HttpMethod.PUT)
				.allowedMethod(HttpMethod.DELETE)
				.maxAgeSeconds(1800));

		router.route().handler(BodyHandler.create());

		router.post("/api/labels").blockingHandler(rc -> {
			client.getConnection(res -> {
				if (res.succeeded()) {
					JsonObject labelEntity = rc.getBodyAsJson();
					SQLConnection conn = res.result();
					conn.updateWithParams("INSERT INTO LABEL(label_name) VALUES (?)",
							new JsonArray().add(labelEntity.getString("labelName")), r -> {
								conn.close();
								if (r.succeeded()) {
									rc.response().end("{}");
								} else {
									rc.response().setStatusCode(500).setStatusMessage(r.cause().getLocalizedMessage()).end();
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
					SQLConnection conn = res.result();
					conn.query("SELECT id, label_name labelName FROM LABEL ORDER BY label_name ASC", r -> {
						conn.close();
						if (r.succeeded()) {
							rc.response().end(Json.encodePrettily(r.result().getRows()));
						} else {
							rc.response().setStatusCode(500).setStatusMessage(r.cause().getLocalizedMessage()).end();
						}
					});
				} else {
					rc.response().setStatusCode(500).end();
				}
			});
		});

		router.delete("/api/labels/:id").blockingHandler(rc -> {
			client.getConnection(res -> {
				if (res.succeeded()) {
					SQLConnection conn = res.result();
					conn.updateWithParams("delete from LABEL where id = ?",
							new JsonArray().add(rc.request().getParam("id")), r -> {
								conn.close();
								if (r.succeeded()) {
									rc.response().end();
								} else {
									rc.response().setStatusCode(500).setStatusMessage(r.cause().getLocalizedMessage())
											.end();
								}
							});
				} else {
					rc.response().setStatusCode(500).end();
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
