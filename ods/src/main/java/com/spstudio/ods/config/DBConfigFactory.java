package com.spstudio.ods.config;

import io.vertx.core.json.JsonObject;

public abstract class DBConfigFactory {

	public abstract JsonObject createDatabaseConfig();
}
