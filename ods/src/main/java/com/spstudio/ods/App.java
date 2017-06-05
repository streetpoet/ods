package com.spstudio.ods;


import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jdbc.JDBCAuth;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.TemplateHandler;
import io.vertx.ext.web.templ.ThymeleafTemplateEngine;

public class App extends AbstractVerticle {

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		
		JsonObject config = new JsonObject()
				.put("driver_class", "com.mysql.jdbc.Driver")
				.put("url", "localhost:3306")
				.put("user", "vertx")
				.put("password", "password")
				.put("database", "testdb")
				.put("charset", "utf-8");
		JDBCClient client = JDBCClient.createShared(vertx, config);
		JDBCAuth authProvider = JDBCAuth.create(vertx, client);
		client.getConnection(res -> {
			if (res.succeeded()) {
				res.result().query("select 1 from duel", r -> {
					System.out.println(r.result().getResults().get(0));
				});
			}
		});
		
		HttpServer server = vertx.createHttpServer();

		Router router = Router.router(vertx);

		StaticHandler staticHandler = StaticHandler.create();
		staticHandler.setDefaultContentEncoding("utf-8");
		router.route("/").handler(staticHandler);
		router.route("/static/*").handler(staticHandler);
		
//		AuthHandler authHandler = buildJwtAuthHandler();
//		router.route("/business/*").handler(authHandler);

		router.route().handler(routingContext -> {
			routingContext.put("welcome", "hello, streetpoet");
			routingContext.next();
		});

		// template engine configuration
		ThymeleafTemplateEngine engine = ThymeleafTemplateEngine.create();
		TemplateHandler handler = TemplateHandler.create(engine);

		router.get("/business/*").handler(handler);

		server.requestHandler(router::accept).listen(8080);
	}

//	protected AuthHandler buildJwtAuthHandler() {
//		JsonObject config = new JsonObject().put("keyStore",
//				new JsonObject().put("path", "keystore.jceks").put("type", "jceks").put("password", "213231"));
//		JWTAuth provider = JWTAuth.create(vertx, config);
//		String token = provider.generateToken(new JsonObject().put("sub", "1"), new JWTOptions());
//		System.out.println(String.format("token: %s", token));
//		return JWTAuthHandler.create(provider);
//	}
}
