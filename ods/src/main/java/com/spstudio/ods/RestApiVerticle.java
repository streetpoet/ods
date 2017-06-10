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
import io.vertx.ext.auth.jdbc.JDBCAuth;
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
		Router apiRouter = Router.router(vertx);
		router.mountSubRouter("/api", apiRouter);

		DBConfigFactory dbConfigFactory = new MySQLDBConfigFactory();
		JDBCClient client = JDBCClient.createShared(vertx, dbConfigFactory.createDatabaseConfig());
		JDBCAuth jdbcAuth = JDBCAuth.create(vertx, client);

		apiRouter.route().handler(CorsHandler.create("http://localhost:8080")
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

		apiRouter.route().handler(BodyHandler.create());

		apiRouter.post("/labels").blockingHandler(rc -> {
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

		apiRouter.get("/labels").blockingHandler(rc -> {
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

		apiRouter.delete("/labels/:id").blockingHandler(rc -> {
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
		
		apiRouter.post("/users").blockingHandler(rc -> {
			client.getConnection(res -> {
				if (res.succeeded()) {
					JsonObject userEntity = rc.getBodyAsJson();
					String salt = jdbcAuth.generateSalt();
					String hashedPassword = jdbcAuth.computeHash(userEntity.getString("password"), salt);
					JsonArray jsonParam = new JsonArray()
							.add(userEntity.getString("loginId"))
							.add(userEntity.getString("nickname"))
							.add(hashedPassword)
							.add(salt)
							.add(userEntity.getString("email"))
							.add(Integer.parseInt(userEntity.getString("labelId")));
					SQLConnection conn = res.result();
					conn.updateWithParams("insert into USER (username, nickname, password, password_salt, email, label_id) values (?, ?, ?, ?, ?, ?)",
							jsonParam, r -> {
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
		
		apiRouter.get("/users").blockingHandler(rc -> {
			client.getConnection(res -> {
				if (res.succeeded()) {
					SQLConnection conn = res.result();
					conn.query("SELECT u.id, username, nickname, email, LABEL.label_name labelName FROM USER u, LABEL WHERE u.label_id = LABEL.id ORDER BY ID ASC", r -> {
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
		
		apiRouter.delete("/users/:id").blockingHandler(rc -> {
			int deleteId = Integer.parseInt(rc.request().getParam("id"));
			if (deleteId <= 1) {
				rc.response().setStatusCode(500).setStatusMessage("forbidden to delete system administrator").end();
				return;
			}
			client.getConnection(res -> {
				if (res.succeeded()) {
					SQLConnection conn = res.result();
					conn.updateWithParams("delete from USER where id = ?",
							new JsonArray().add(deleteId), r -> {
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
