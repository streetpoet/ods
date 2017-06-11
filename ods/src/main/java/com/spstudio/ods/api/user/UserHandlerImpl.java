package com.spstudio.ods.api.user;

import com.spstudio.ods.OdsUtil;

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
				conn.setAutoCommit(false, autoCommitHandler -> {
					if (autoCommitHandler.succeeded()) {
						conn.updateWithParams(
								"delete from USER_ROLES where username = (select username from USER where id = ?)",
								new JsonArray().add(deleteId), r -> {
									if (r.succeeded()) {
										conn.updateWithParams("delete from USER where id = ?",
												new JsonArray().add(deleteId), r2 -> {
													if (r2.succeeded()) {
														OdsUtil.commitWithEndResponse(conn, rc, new JsonObject());
													}else {
														OdsUtil.rollbackWithEndResponse(conn, rc, r2);
													}
												});
									} else {
										OdsUtil.rollbackWithEndResponse(conn, rc, r);
									}
								});
					} else {
						OdsUtil.rollbackWithEndResponse(conn, rc, autoCommitHandler);
					}
				});
			} else {
				OdsUtil.errorEndResponse(rc, res);
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
				String loginId = userEntity.getString("loginId");
				JsonArray jsonParam = new JsonArray().add(loginId).add(userEntity.getString("nickname"))
						.add(hashedPassword).add(salt).add(userEntity.getString("email"))
						.add(Integer.parseInt(userEntity.getString("labelId")));
				SQLConnection conn = res.result();
				conn.setAutoCommit(false, autoCommitHandler -> {
					if (autoCommitHandler.succeeded()) {
						conn.updateWithParams(
								"insert into USER (username, nickname, password, password_salt, email, label_id) values (?, ?, ?, ?, ?, ?)",
								jsonParam, r -> {
									if (r.succeeded()) {
										conn.updateWithParams("insert into USER_ROLES (username, role) values (?, ?)",
												new JsonArray().add(loginId).add("user"), r2 -> {
													if (r2.succeeded()) {
														OdsUtil.commitWithEndResponse(conn, rc, new JsonObject());
													} else {
														OdsUtil.rollbackWithEndResponse(conn, rc, r2);
													}
												});
									} else {
										OdsUtil.rollbackWithEndResponse(conn, rc, r);
									}
								});
					} else {
						OdsUtil.rollbackWithEndResponse(conn, rc, autoCommitHandler);
					}
				});

			} else {
				OdsUtil.errorEndResponse(rc, res);
			}
		});
	}

}
