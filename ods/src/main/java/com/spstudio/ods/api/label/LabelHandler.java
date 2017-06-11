package com.spstudio.ods.api.label;

import io.vertx.ext.web.RoutingContext;

public interface LabelHandler {
	
	public void createLabel(RoutingContext rc);
	public void readLabels(RoutingContext rc);
	public void deleteLabel(RoutingContext rc);
}
