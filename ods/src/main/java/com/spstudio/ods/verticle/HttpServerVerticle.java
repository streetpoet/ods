package com.spstudio.ods.verticle;

import com.spstudio.ods.factory.db.DBConfigFactory;
import com.spstudio.ods.factory.db.MySQLDBConfigFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.auth.jdbc.JDBCAuth;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.AuthHandler;
import io.vertx.ext.web.handler.BasicAuthHandler;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.TemplateHandler;
import io.vertx.ext.web.handler.UserSessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.templ.ThymeleafTemplateEngine;

public class HttpServerVerticle extends AbstractVerticle {

	@Override
	public void start(Future<Void> startFuture) throws Exception {

		HttpServerOptions options = new HttpServerOptions();
		options.setLogActivity(true);
		HttpServer server = vertx.createHttpServer();

		Router router = Router.router(vertx);

		DBConfigFactory dbConfigFactory = new MySQLDBConfigFactory();
		JDBCClient client = JDBCClient.createShared(vertx, dbConfigFactory.createDatabaseConfig());
		JDBCAuth authProvider = JDBCAuth.create(vertx, client);

		router.route().handler(CookieHandler.create());
		router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
		router.route().handler(UserSessionHandler.create(authProvider));

		StaticHandler staticHandler = StaticHandler.create();
		staticHandler.setDefaultContentEncoding("utf-8");
		router.route("/").handler(staticHandler);
		router.route("/static/*").handler(staticHandler);

		router.route("/business/*").handler(BodyHandler.create());

		AuthHandler authHandler = BasicAuthHandler.create(authProvider);
		router.route("/business/*").handler(authHandler);

		router.route("/logout").handler(rc -> {
			rc.clearUser();
			rc.response().putHeader("location", "/").setStatusCode(302).end();
		});

		router.route("/business/*").handler(rc -> {
			rc.response().putHeader("Pragma", "no-cache");
			rc.response().putHeader("Expires", "0");
			rc.response().putHeader("Cache-Control", "no-cache, no-store, must-revalidate");
			rc.put("user", rc.user().principal().getString("username"));
			rc.next();
		});

		// Authorization
		router.get("/business/label.html").handler(rc -> {
			rc.user().isAuthorised("role:admin", handler -> {
				if (handler.succeeded()) {
					if (!handler.result()) {
						rc.reroute("/business/dashboard.html");
						return;
					}
				}
				rc.next();
			});
		});
		router.get("/business/user.html").handler(rc -> {
			rc.user().isAuthorised("role:admin", handler -> {
				if (handler.succeeded()) {
					if (!handler.result()) {
						rc.reroute("/business/dashboard.html");
						return;
					}
				}
				rc.next();
			});
		});

		// template engine configuration
		ThymeleafTemplateEngine engine = ThymeleafTemplateEngine.create();
		TemplateHandler handler = TemplateHandler.create(engine);
		router.route("/business/*").handler(handler);

		server.requestHandler(router::accept).listen(8080, r -> {
			if (r.succeeded()) {
				System.out.println("Server is now listening at 8080!");
			} else {
				System.out.println("Failed to bind!");
			}
		});

		startFuture.complete();
	}

}
