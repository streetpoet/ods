package com.spstudio.ods.factory.db;

import io.vertx.core.json.JsonObject;

public class MySQLDBConfigFactory extends DBConfigFactory {

	@Override
	public JsonObject createDatabaseConfig() {
		JsonObject config = new JsonObject()
				.put("driver_class", "com.mysql.jdbc.Driver")
				.put("url", "jdbc:mysql://localhost/testdb")
				.put("user", "vertx").put("password", "password")
				.put("charset", "utf-8");
		return config;
	}

}
