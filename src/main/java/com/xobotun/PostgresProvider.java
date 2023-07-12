package com.xobotun;

import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainerProvider;

public class PostgresProvider {
    private static final String LATEST_PG = "15.3";

    public static JdbcDatabaseContainer getTemplate() {
        var template = new PostgreSQLContainerProvider().newInstance(LATEST_PG);

        template.withInitScript("init.sql");

        return template;
    }

}
