package com.spstudio.ods.factory.db;

import io.vertx.core.json.JsonObject;

public abstract class DBConfigFactory {

	public abstract JsonObject createDatabaseConfig();
}
