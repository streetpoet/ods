package com.spstudio.ods;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jdbc.JDBCAuth;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.AuthHandler;
import io.vertx.ext.web.handler.BasicAuthHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.TemplateHandler;
import io.vertx.ext.web.handler.UserSessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.templ.ThymeleafTemplateEngine;

public class App extends AbstractVerticle {

	@Override
	public void start(Future<Void> startFuture) throws Exception {

		HttpServer server = vertx.createHttpServer();
		
		Router router = Router.router(vertx);

		JsonObject config = new JsonObject()
				.put("driver_class", "com.mysql.jdbc.Driver")
				.put("url", "jdbc:mysql://localhost/testdb")
				.put("user", "vertx")
				.put("password", "password");
		JDBCClient client = JDBCClient.createShared(vertx, config);
		JDBCAuth authProvider = JDBCAuth.create(vertx, client);
		
		router.route().handler(CookieHandler.create());
		router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
		router.route().handler(UserSessionHandler.create(authProvider));
		
		AuthHandler authHandler = BasicAuthHandler.create(authProvider);
		router.route("/business/*").handler(authHandler);
		
		StaticHandler staticHandler = StaticHandler.create();
		staticHandler.setDefaultContentEncoding("utf-8");
		router.route("/").handler(staticHandler);
		router.route("/static/*").handler(staticHandler);
		
		router.route("/logout").handler(routingContext -> {
			routingContext.clearUser();
			routingContext.reroute("/");
		});
		
		// Authorization
		router.route("/business/label").handler(rc -> {
			rc.user().isAuthorised("role:admin", handler -> {
				if (handler.failed()) {
					rc.reroute("/");
				}
			});
		});

		// template engine configuration
		ThymeleafTemplateEngine engine = ThymeleafTemplateEngine.create();
		TemplateHandler handler = TemplateHandler.create(engine); 
		router.route("/business/*").handler(handler);

		server.requestHandler(router::accept).listen(8080);
	}
	
//	router.route("/gen").blockingHandler(routingContext -> {
//	JsonObject config = new JsonObject() 
//			.put("driver_class", "com.mysql.jdbc.Driver")
//			.put("url", "jdbc:mysql://localhost/testdb")
//			.put("user", "vertx")
//			.put("password", "password");
//	JDBCClient client = JDBCClient.createShared(vertx, config);
//	JDBCAuth authProvider = JDBCAuth.create(vertx, client);
//	String salt = authProvider.generateSalt();
//	String hash = authProvider.computeHash("213231", salt);
//	
//	client.getConnection(res -> {
//		if (res.succeeded()) {
//			res.result().updateWithParams("INSERT INTO user VALUES (?, ?, ?)", new JsonArray().add("streetpoet").add(hash).add(salt), r -> {
//				if (r.succeeded()) {
//					System.out.println("insert success!");
//				}else {
//					System.out.println(r.cause().getMessage());
//				}
//				routingContext.response().end();
//			});
//		}
//	});
//});
}
