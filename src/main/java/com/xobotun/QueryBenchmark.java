package com.xobotun;

import com.google.common.collect.Iterables;
import org.jooq.Record;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.util.postgres.PostgresDSL;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.JdbcDatabaseContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.xobotun.DatabasePopulator.DEFAULT_PASSWORD;
import static com.xobotun.DatabasePopulator.DEFAULT_USER;
import static com.xobotun.jooq.public_.tables.ProductRecommendation.PRODUCT_RECOMMENDATION;
import static org.jooq.impl.DSL.value;


@BenchmarkMode({Mode.AverageTime, Mode.Throughput})
@Warmup(iterations = 0, time = 2)
@Measurement(iterations = 1, time = 1)
@Fork(1)
//@Threads(4)
public class QueryBenchmark {
    private static final Logger logger = LoggerFactory.getLogger(QueryBenchmark.class);

    @State(Scope.Benchmark)
    public static class DatabaseState {
        // How much data is within the database: 500k, 5M, 50M
//        @Param({ "500000", "5000000"/*, "50000000"*/ })
        public int productRecommendationNumber = 1000;
        // How much product_ids is passed: 10k, 100k, 1M
//        @Param({ "10000", "100000", "1000000" })
        public int queryListSize = 100;
        private List<Set<Integer>> queryLists;
        private int attemptCount = 0;

        private final JdbcDatabaseContainer container = PostgresProvider.getTemplate();
        private Connection connection;

        @Setup
        public void setup() throws SQLException {
            queryLists = DataListGenerator.generateSets(queryListSize, DatabasePopulator.getProductCount(productRecommendationNumber));

            container.start();
            logger.debug("Started PG container");
            DatabasePopulator.populateWithData(container, productRecommendationNumber);

            connection = DriverManager.getConnection(container.getJdbcUrl(), DEFAULT_USER, DEFAULT_PASSWORD);
        }

        @TearDown
        public void tearDown() throws SQLException {
            connection.close();

            container.stop();
            logger.debug("Stopped PG container");
        }

        private Set<Integer> getNextList() {
            return queryLists.get(attemptCount++ % queryLists.size());
        }
    }

    @Benchmark
    public void naiveQueryApproach(DatabaseState state, Blackhole blackhole) {
        DSLContext ctx = DSL.using(state.connection, SQLDialect.POSTGRES);

        Set<Integer> notIds = state.getNextList();
        Result<Record1<Integer>> result = ctx.select(PRODUCT_RECOMMENDATION.ID).from(PRODUCT_RECOMMENDATION)
                .where(PRODUCT_RECOMMENDATION.PRODUCT_ID1.notIn(notIds)).fetch();
        if (logger.isDebugEnabled()) logger.debug("Fetched {} records on attempt {}", result.size(), state.attemptCount - 1);

        blackhole.consume(result);
    }

    @Benchmark
    public void concatenatedStringApproach(DatabaseState state, Blackhole blackhole) {
        DSLContext ctx = DSL.using(state.connection, SQLDialect.POSTGRES);

        Set<Integer> notIds = state.getNextList();
        String ids = notIds.stream().map(Object::toString).collect(Collectors.joining(","));
        Result<Record1<Integer>> result = ctx.select(PRODUCT_RECOMMENDATION.ID).from(PRODUCT_RECOMMENDATION)
                .where(
                        DSL.condition("not ({0} = any({1}::integer[]))", PRODUCT_RECOMMENDATION.PRODUCT_ID1, PostgresDSL.stringToArray(ids, value(",")))
                ).fetch();
        if (logger.isDebugEnabled()) logger.debug("Fetched {} records on attempt {}", result.size(), state.attemptCount - 1);

        blackhole.consume(result);
    }

    @Benchmark
    public void temporaryTableNaiveApproach(DatabaseState state, Blackhole blackhole) {
        DSLContext ctx = DSL.using(state.connection, SQLDialect.POSTGRES);
        Set<Integer> notIds = state.getNextList();

        Table<Record> tmpTable = DSL.table(DSL.name("excluded_ids"));
        Field<Integer> tmpColumn = DSL.field(DSL.name("id"), Integer.class);

        AtomicReference<Result<Record1<Integer>>> resultRef = new AtomicReference<>();
        ctx.transaction(trx -> {
            trx.dsl().createTemporaryTable(tmpTable).column(tmpColumn).execute();
            for (Integer i : notIds) trx.dsl().insertInto(tmpTable, tmpColumn).values(i).execute();

            Result<Record1<Integer>> result = trx.dsl().select(PRODUCT_RECOMMENDATION.ID).from(PRODUCT_RECOMMENDATION)
                    .where(PRODUCT_RECOMMENDATION.PRODUCT_ID1.notIn(trx.dsl().select(tmpColumn).from(tmpTable))).fetch();

            trx.dsl().dropTemporaryTable(tmpTable).execute();
            resultRef.set(result);
        });
        if (logger.isDebugEnabled()) logger.debug("Fetched {} records on attempt {}", resultRef.get().size(), state.attemptCount - 1);

        blackhole.consume(resultRef.get());
    }

    @Benchmark
    public void temporaryTableConcatenatedStringApproach(DatabaseState state, Blackhole blackhole) {
        DSLContext ctx = DSL.using(state.connection, SQLDialect.POSTGRES);
        Set<Integer> notIds = state.getNextList();
        String ids = notIds.stream().map(Object::toString).collect(Collectors.joining(","));

        Table<Record> tmpTable = DSL.table(DSL.name("excluded_ids"));
        Field<Integer> tmpColumn = DSL.field(DSL.name("id"), Integer.class);

        AtomicReference<Result<Record1<Integer>>> resultRef = new AtomicReference<>();
        ctx.transaction(trx -> {
            trx.dsl().createTemporaryTable(tmpTable).column(tmpColumn).execute();
            // Failed to figure out how to use unnest with jooq. :/
            trx.dsl().query("insert into excluded_ids(id) select unnest(string_to_array(?, ','))::integer", ids).execute();

            Result<Record1<Integer>> result = trx.dsl().select(PRODUCT_RECOMMENDATION.ID).from(PRODUCT_RECOMMENDATION)
                    .where(PRODUCT_RECOMMENDATION.PRODUCT_ID1.notIn(trx.dsl().select(tmpColumn).from(tmpTable))).fetch();

            trx.dsl().dropTemporaryTable(tmpTable).execute();
            resultRef.set(result);
        });
        if (logger.isDebugEnabled()) logger.debug("Fetched {} records on attempt {}", resultRef.get().size(), state.attemptCount - 1);

        blackhole.consume(resultRef.get());
    }

    @Benchmark
    public void temporaryTableBatchedApproach(DatabaseState state, Blackhole blackhole) {
        DSLContext ctx = DSL.using(state.connection, SQLDialect.POSTGRES);
        Set<Integer> notIds = state.getNextList();
        int batchSize = 256; // 4 bytes * 500 = 1 KiB. Good for network, I think.
        Iterable<List<Integer>> batched = Iterables.partition(notIds, batchSize);

        Table<Record> tmpTable = DSL.table(DSL.name("excluded_ids"));
        Field<Integer> tmpColumn = DSL.field(DSL.name("id"), Integer.class);

        AtomicReference<Result<Record1<Integer>>> resultRef = new AtomicReference<>();
        ctx.transaction(trx -> {
            trx.dsl().createTemporaryTable(tmpTable).column(tmpColumn).execute();

            for (List<Integer> batch : batched) {
                BatchBindStep batchQuery = trx.dsl().batch(trx.dsl().insertInto(tmpTable, tmpColumn).values((Integer) null));
                for (Integer i : batch) batchQuery = batchQuery.bind(i);
                batchQuery.execute();
            }

            Result<Record1<Integer>> result = trx.dsl().select(PRODUCT_RECOMMENDATION.ID).from(PRODUCT_RECOMMENDATION)
                    .where(PRODUCT_RECOMMENDATION.PRODUCT_ID1.notIn(trx.dsl().select(tmpColumn).from(tmpTable))).fetch();

            trx.dsl().dropTemporaryTable(tmpTable).execute();
            resultRef.set(result);
        });
        if (logger.isDebugEnabled()) logger.debug("Fetched {} records on attempt {}", resultRef.get().size(), state.attemptCount - 1);

        blackhole.consume(resultRef.get());
    }

}
