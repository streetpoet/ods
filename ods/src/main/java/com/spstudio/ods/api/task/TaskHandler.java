package com.spstudio.ods.api.task;

import io.vertx.ext.web.RoutingContext;

public interface TaskHandler {

	public void createTask(RoutingContext rc);

	public void readTasksByUser(RoutingContext rc);
}
