## sql-list-query-test-task

### The task:
 In an SQL database, there is a table called `product_recommendation` with columns `id`, `product_id1`, 
 and `product_id2`.
 The `id` column is the primary key, and the other two columns are foreign keys referencing the `product` table
 (the specific structure of which is not relevant for this task). All columns are of type integer.
 The `product_recommendation` table contains several million records.

 In a Java application, there is an array or any collection (of your choice) containing 100,000 product IDs
 (also integers). The goal is to obtain a list of all recommendations (rows from the `product_recommendation` table)
 where `product_id1` is NOT in this array/collection.

 Outline the main steps for solving this task and provide the main SQL query that needs to be executed in the process.

### Train of thoughts:

1. There are two solutions I can name right off the bat: 
   1. Load the collection into a `select * from product_recommendation where product_id1 not in (:list)` and 
      pray for the database to parse it, not to raise exceptions about arguments list being too long, etc.
   2. Utilise some kind of temporary table, and run a query like `select pr.* from product_recommendation pr join 
      tmp_table tt on pr.product_id1 <> tt.id`. This should work faster, but we still need some time to populate 
      the temporary table. It should go faster with batching, though.
2. Various frameworks may work with different efficiency. And they each will require tuning. 
3. Look up any other viable options in the Internet:
   1. [Baeldung](https://www.baeldung.com/spring-jdbctemplate-in-list) mentions a temporary table.
   2. [An answer on SO](https://stackoverflow.com/a/32561721/7643283) mentions passing list as a concatenated string. 
4. Ask ChatGPT-4 for lulz: 
    1. Happened to be down at the moment
5. As I have no work and 2 days to complete this test task, let's try to compare the approaches.
6. I will use PostgreSQL as the database. Using different RDBMS is also interesting, but this will bloat out 
   the task I want to do too much.

### Difficulties

1. It was nasty to write JooQ queries with PostgreSQL-specific syntax. 
   For example in `insert into excluded_ids(id) select unnest(string_to_array(?, ','))::integer` the `unnest` operator
   is acting weird in Postgres and thus is badly supported by JooQ. Or I haven't figured the right way to use it an spent
   some hours figuring it out.
2. Generating random 5M data took about a minute on my machine, so I decide not to run 50M tests. I believe this time 
   can be improved by a lot by generating the data first, and applying the limitations
   (pk and fk indices and fk constraints) after the data was created, but I simply don't have enough time 
   to polish this test.

### Conclusion

The first run with 500k products, 5M recommendations and 100k query elements took 11:32 time units and returned 
the following result:

```text
Benchmark                                                 Mode  Cnt   Score   Error  Units
QueryBenchmark.naiveQueryApproach                        thrpt        0.445          ops/s
QueryBenchmark.concatenatedStringApproach                thrpt        0.498          ops/s
QueryBenchmark.temporaryTableNaiveApproach               thrpt        0.023          ops/s
QueryBenchmark.temporaryTableConcatenatedStringApproach  thrpt        0.364          ops/s
QueryBenchmark.temporaryTableBatchedApproach             thrpt        0.294          ops/s
QueryBenchmark.naiveQueryApproach                         avgt        2.442           s/op
QueryBenchmark.concatenatedStringApproach                 avgt        2.172           s/op
QueryBenchmark.temporaryTableNaiveApproach                avgt       46.229           s/op
QueryBenchmark.temporaryTableConcatenatedStringApproach   avgt        2.827           s/op
QueryBenchmark.temporaryTableBatchedApproach              avgt        3.950           s/op
```

It is weird to naive approach with a temporary table to be so fast, but this benchmark run had only 1 run 
and none warmups. I used it to determine what benchmark mode will suit this test the best and decided it was `AvgTime`.

