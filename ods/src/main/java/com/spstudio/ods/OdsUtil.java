package com.spstudio.ods;

import io.vertx.core.AsyncResult;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.RoutingContext;

public class OdsUtil {

	public static void errorEndResponse(RoutingContext rc, AsyncResult<?> r) {
		rc.response().setStatusCode(500).setStatusMessage(r.cause().getLocalizedMessage()).end();
	}

	public static void commitWithEndResponse(SQLConnection conn, RoutingContext rc, JsonObject jsonResponse) {
		conn.commit(r -> {
			if (r.succeeded()) {
				conn.close();
				rc.response().end(Json.encodePrettily(jsonResponse));
			} else {
				rollbackWithEndResponse(conn, rc, r);
			}
		});
	}

	public static void rollbackWithEndResponse(SQLConnection conn, RoutingContext rc, AsyncResult<?> r) {
		conn.rollback(r1 -> {
			conn.close(r2 -> {
				OdsUtil.errorEndResponse(rc, r);
			});
		});
	}
}
