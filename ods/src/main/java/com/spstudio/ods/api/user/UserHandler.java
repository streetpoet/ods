package com.spstudio.ods.api.user;

import io.vertx.ext.web.RoutingContext;

public interface UserHandler {
	
	public void createUser(RoutingContext rc);
	public void readUsers(RoutingContext rc);
	public void deleteUser(RoutingContext rc);
	public void readUsersByLabel(RoutingContext rc);
}
