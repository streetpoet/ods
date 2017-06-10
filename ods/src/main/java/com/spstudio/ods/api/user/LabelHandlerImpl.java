package com.spstudio.ods.api.user;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.RoutingContext;

public class LabelHandlerImpl implements LabelHandler {

	private JDBCClient client;

	public LabelHandlerImpl(JDBCClient client) {
		this.client = client;
	}

	@Override
	public void createLabel(RoutingContext rc) {
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
	public void readLabels(RoutingContext rc) {
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
	}

	@Override
	public void deleteLabel(RoutingContext rc) {
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
	}

}
