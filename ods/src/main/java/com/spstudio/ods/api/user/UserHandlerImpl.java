package com.spstudio.ods.api.user;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jdbc.JDBCAuth;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.RoutingContext;

public class UserHandlerImpl implements UserHandler {

	private JDBCClient client;
	private JDBCAuth jdbcAuth;

	public UserHandlerImpl(JDBCClient client, JDBCAuth jdbcAuth) {
		this.client = client;
		this.jdbcAuth = jdbcAuth;
	}

	@Override
	public void readUsers(RoutingContext rc) {
		client.getConnection(res -> {
			if (res.succeeded()) {
				SQLConnection conn = res.result();
				conn.query(
						"SELECT u.id, username, nickname, email, LABEL.label_name labelName FROM USER u, LABEL WHERE u.label_id = LABEL.id ORDER BY ID ASC",
						r -> {
							conn.close();
							if (r.succeeded()) {
								rc.response().end(Json.encodePrettily(r.result().getRows()));
							} else {
								rc.response().setStatusCode(500).setStatusMessage(r.cause().getLocalizedMessage())
										.end();
							}
						});
			} else {
				rc.response().setStatusCode(500).end();
			}
		});
	}

	@Override
	public void deleteUser(RoutingContext rc) {
		int deleteId = Integer.parseInt(rc.request().getParam("id"));
		if (deleteId <= 1) {
			rc.response().setStatusCode(500).setStatusMessage("forbidden to delete system administrator").end();
			return;
		}
		client.getConnection(res -> {
			if (res.succeeded()) {
				SQLConnection conn = res.result();
				conn.updateWithParams("delete from USER where id = ?", new JsonArray().add(deleteId), r -> {
					conn.close();
					if (r.succeeded()) {
						rc.response().end();
					} else {
						rc.response().setStatusCode(500).setStatusMessage(r.cause().getLocalizedMessage()).end();
					}
				});
			} else {
				rc.response().setStatusCode(500).end();
			}
		});
	}

	@Override
	public void createUser(RoutingContext rc) {
		client.getConnection(res -> {
			if (res.succeeded()) {
				JsonObject userEntity = rc.getBodyAsJson();
				String salt = jdbcAuth.generateSalt();
				String hashedPassword = jdbcAuth.computeHash(userEntity.getString("password"), salt);
				JsonArray jsonParam = new JsonArray().add(userEntity.getString("loginId"))
						.add(userEntity.getString("nickname")).add(hashedPassword).add(salt)
						.add(userEntity.getString("email")).add(Integer.parseInt(userEntity.getString("labelId")));
				SQLConnection conn = res.result();
				conn.updateWithParams(
						"insert into USER (username, nickname, password, password_salt, email, label_id) values (?, ?, ?, ?, ?, ?)",
						jsonParam, r -> {
							conn.close();
							if (r.succeeded()) {
								rc.response().end("{}");
							} else {
								rc.response().setStatusCode(500).setStatusMessage(r.cause().getLocalizedMessage())
										.end();
							}
						});
			} else {
				rc.response().setStatusCode(500).end();
			}
		});
	}

}
