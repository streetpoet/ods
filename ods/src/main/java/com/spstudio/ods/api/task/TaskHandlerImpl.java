package com.spstudio.ods.api.task;

import java.sql.Time;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.spstudio.ods.OdsUtil;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.RoutingContext;

public class TaskHandlerImpl implements TaskHandler {

	private JDBCClient client;

	public TaskHandlerImpl(JDBCClient client) {
		this.client = client;
	}
	
	@Override
	public void createTask(RoutingContext rc) {
		client.getConnection(res -> {
			if (res.succeeded()) {
				
				JsonObject taskEntity = rc.getBodyAsJson();
				String detail = taskEntity.getString("detail");
				int assignerId = Integer.parseInt(taskEntity.getString("assignerId"));
				LocalDateTime date = LocalDateTime.now();
				String dt = date.format(DateTimeFormatter.ISO_DATE_TIME);
				
				SQLConnection conn = res.result();
				conn.updateWithParams("insert into TASK (detail, assigner_id, created_time, assigned_time) values (?, ?, ?, ?)",
						new JsonArray().add(detail).add(assignerId).add(dt).add(dt), r -> {
							conn.close();
							if (r.succeeded()) {
								rc.response().end(Json.encodePrettily(new JsonObject()));
							} else {
								OdsUtil.errorEndResponse(rc, r);
							}
						});
			} else {
				rc.response().setStatusCode(500).end();
			}
		});

	}

	@Override
	public void readTasksByUser(RoutingContext rc) {
		// TODO Auto-generated method stub

	}

}
