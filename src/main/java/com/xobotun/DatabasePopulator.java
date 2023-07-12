package com.xobotun;

import org.testcontainers.containers.JdbcDatabaseContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DatabasePopulator {
    private static final String DEFAULT_USER = "test";
    private static final String DEFAULT_PASSWORD = "test";

    public static void populateWithData(JdbcDatabaseContainer container, int dataSize) {
        int productCount = dataSize / 10;

        try (Connection conn = DriverManager.getConnection(container.getJdbcUrl(), DEFAULT_USER, DEFAULT_PASSWORD)) {
            try (Statement stat = conn.createStatement()) {
                stat.execute("insert into product select from generate_series(1, %d);".formatted(productCount));
            }
            try (Statement stat = conn.createStatement()) {
                stat.execute(("insert into product_recommendation (product_id1, product_id2) " +
                              "select floor(random() * %d + 1)::int, floor(random() * %d + 1)::int " +
                              "from generate_series(1, %d);").formatted(productCount, productCount, dataSize));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }
}
