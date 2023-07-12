package com.xobotun;

import org.openjdk.jmh.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.JdbcDatabaseContainer;

import java.util.List;
import java.util.Set;


@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 1, time = 2)
@Measurement(iterations = 2, time = 2)
@Fork(1)
//@Threads(4)
public class QueryBenchmark {
    private static final Logger logger = LoggerFactory.getLogger(QueryBenchmark.class);

    @State(Scope.Benchmark)
    public static class DatabaseState {
        // How much data is within the database: 500k, 5M, 50M
        @Param({ "500000", "5000000", "50000000" })
        public int productRecommendationNumber;
        // How much product_ids is passed: 10k, 100k, 1M
        @Param({ "10000", "100000", "1000000" })
        public int queryListSize;
        private List<Set<Integer>> queryLists;

        private final JdbcDatabaseContainer container = PostgresProvider.getTemplate();

        @Setup
        public void setup() {
            queryLists = DataListGenerator.generateSets(queryListSize, productRecommendationNumber);

            container.start();
            logger.debug("Started PG container");
            DatabasePopulator.populateWithData(container, productRecommendationNumber);
        }

        @TearDown
        public void tearDown() {
            container.stop();
            logger.debug("Stopped PG container");
        }
    }

    @Benchmark
    @OperationsPerInvocation(4)
    public void testMethod(DatabaseState state) {
        // This is a demo/sample template for building your JMH benchmarks. Edit as needed.
        // Put your benchmark code here.
    }

}
