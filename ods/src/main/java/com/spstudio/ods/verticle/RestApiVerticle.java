package com.spstudio.ods.verticle;

import com.spstudio.ods.api.label.LabelHandler;
import com.spstudio.ods.api.label.LabelHandlerImpl;
import com.spstudio.ods.api.task.TaskHandler;
import com.spstudio.ods.api.task.TaskHandlerImpl;
import com.spstudio.ods.api.user.UserHandler;
import com.spstudio.ods.api.user.UserHandlerImpl;
import com.spstudio.ods.factory.cors.CorsHandlerFactory;
import com.spstudio.ods.factory.cors.OdsApiCorsHandlerFactory;
import com.spstudio.ods.factory.db.DBConfigFactory;
import com.spstudio.ods.factory.db.MySQLDBConfigFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.auth.jdbc.JDBCAuth;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class RestApiVerticle extends AbstractVerticle {

	@Override
	public void start(Future<Void> startFuture) throws Exception {

		HttpServer server = vertx.createHttpServer();

		Router router = Router.router(vertx);
		Router apiRouter = Router.router(vertx);
		router.mountSubRouter("/api", apiRouter);

		DBConfigFactory dbConfigFactory = new MySQLDBConfigFactory();
		JDBCClient client = JDBCClient.createShared(vertx, dbConfigFactory.createDatabaseConfig());
		JDBCAuth jdbcAuth = JDBCAuth.create(vertx, client);

		CorsHandlerFactory corsFactory = new OdsApiCorsHandlerFactory();
		apiRouter.route().handler(corsFactory.createCorsHandler());
		apiRouter.route().handler(BodyHandler.create());

		UserHandler userHandler = new UserHandlerImpl(client, jdbcAuth);
		LabelHandler labelHandler = new LabelHandlerImpl(client);
		TaskHandler taskHandler = new TaskHandlerImpl(client);

		apiRouter.post("/users").blockingHandler(userHandler::createUser);
		apiRouter.get("/users").blockingHandler(userHandler::readUsers);
		apiRouter.get("/users/label/:labelId").blockingHandler(userHandler::readUsersByLabel);
		apiRouter.delete("/users/:id").blockingHandler(userHandler::deleteUser);

		apiRouter.post("/labels").blockingHandler(labelHandler::createLabel);
		apiRouter.get("/labels").blockingHandler(labelHandler::readLabels);
		apiRouter.delete("/labels/:id").blockingHandler(labelHandler::deleteLabel);
		
		apiRouter.post("/tasks").blockingHandler(taskHandler::createTask);
		apiRouter.get("/tasks").blockingHandler(taskHandler::readTasksByUser);

		server.requestHandler(router::accept).listen(8090);
		startFuture.complete();
	}

}
