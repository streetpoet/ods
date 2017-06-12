package com.spstudio.ods.api.task;

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
		String loginId = rc.request().getParam("uid");
		client.getConnection(res -> {
			if (res.succeeded()) {
				SQLConnection conn = res.result();
				StringBuffer sb = new StringBuffer();
				sb.append("SELECT " + 
						"    t.id," + 
						"    t.detail," + 
						"    lb.label_name labelName," + 
						"    u.nickname," + 
						"    t.assigned_time assignedTime," + 
						"    t.status " + 
						"FROM" + 
						"    TASK t," + 
						"    USER u," + 
						"    LABEL lb " + 
						"WHERE" + 
						"    t.assigner_id = u.id" + 
						"    and u.label_id = lb.id");
				conn.queryWithParams("SELECT role FROM USER_ROLES where username = ?", 
						new JsonArray().add(loginId), roleHandler -> {
							if (roleHandler.succeeded()) {
								JsonArray parameters = new JsonArray();
								if (roleHandler.result().getNumRows() == 1) {
									if (!roleHandler.result().getRows().get(0).getString("role").contains("admin")) {
										sb.append(" and u.username = ?");
										parameters.add(loginId);
									}
								}else {
									conn.close();
									rc.response().end(Json.encode(new JsonObject()));
									return;
								}
								sb.append(" ORDER BY t.assigned_time desc");
								conn.queryWithParams(sb.toString(), parameters, r -> {
									if (r.succeeded()) {
										rc.response().end(Json.encodePrettily(r.result().getRows()));
									} else {
										rc.response().setStatusCode(500).setStatusMessage(r.cause().getMessage()).end();
									}
									conn.close();
								});
							}else {
								conn.close();
								OdsUtil.errorEndResponse(rc, roleHandler);
							}
						});
			} else {
				rc.response().setStatusCode(500).end();
			}
		});
	}

}
