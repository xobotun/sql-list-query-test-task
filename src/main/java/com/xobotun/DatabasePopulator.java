package com.xobotun;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.JdbcDatabaseContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DatabasePopulator {
    private static final Logger logger = LoggerFactory.getLogger(DatabasePopulator.class);
    public static final String DEFAULT_USER = "test";
    public static final String DEFAULT_PASSWORD = "test";

    public static void populateWithData(JdbcDatabaseContainer container, int dataSize) {
        int productCount = getProductCount(dataSize);

        try (Connection conn = DriverManager.getConnection(container.getJdbcUrl(), DEFAULT_USER, DEFAULT_PASSWORD)) {
            try (Statement stat = conn.createStatement()) {
                stat.execute("insert into product select from generate_series(1, %d);".formatted(productCount));
                logger.debug("Inserted {} products", productCount);
            }
            try (Statement stat = conn.createStatement()) {
                stat.execute(("insert into product_recommendation (product_id1, product_id2) " +
                              "select floor(random() * %d + 1)::int, floor(random() * %d + 1)::int " +
                              "from generate_series(1, %d);").formatted(productCount, productCount, dataSize));
                logger.debug("Inserted {} product recommendations", dataSize);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static int getProductCount(int dataSize) {
        return dataSize / 10;
    }
}
