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
4. Ask ChatGPT-3.5: was down back then. I ran it after I was done with this task. 
   
   """

   To obtain a list of all recommendations where `product_id1` is not in the given array/collection, you can follow these main steps:
   Convert the array/collection of product IDs to a format suitable for SQL query execution. This can be achieved by either creating a temporary table in the database or constructing a comma-separated string of the product IDs.
   Use the SQL `NOT IN` operator to retrieve the recommendations where product_id1 is not present in the array/collection of product IDs.
   Here is the main SQL query that needs to be executed:
    ```sql
    SELECT *
    FROM product_recommendation
    WHERE product_id1 NOT IN (/* Insert the array/collection of product IDs here */);
    ```
    Ensure that you replace the comment /* Insert the array/collection of product IDs here */ with the actual array or collection of product IDs in the appropriate format.

    """
    
    Which is basically what I have done. 

6. As I have no work and 2 days to complete this test task, let's try to compare the approaches.
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

### Approaches

So far, there are three main approaches:
1. Naive, just pass the collection as is and check `product_id1 not in (:list)`.
2. Pass a concatenated string instead and do the same check.
3. 4\. 5\. Use temporary table and join it against the `product_recommendation` table. This leads to three different
approaches on how to insert data into it, however: naive in loop, with a concatenated string and with batches. 

### Tests

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

This benchmark run had only 1 run 
and none warmups. I used it to determine what benchmark mode will suit this test the best and decided it was `AvgTime`.

Second approach was done on 500k data and 10k, 100k and 1M elements in request. The part done on 5M supposedly gets stuck 
with 100% cpu usage on temporary table approaches, so I decided to eliminate some long runs.

```text
Benchmark                                                (productRecommendationNumber)  (queryListSize)  Mode  Cnt   Score   Error  Units
QueryBenchmark.naiveQueryApproach                                               500000            10000  avgt        0.154           s/op
QueryBenchmark.naiveQueryApproach                                               500000           100000  avgt        0.101           s/op
QueryBenchmark.naiveQueryApproach                                               500000          1000000  avgt        0.088           s/op
QueryBenchmark.concatenatedStringApproach                                       500000            10000  avgt        0.138           s/op
QueryBenchmark.concatenatedStringApproach                                       500000           100000  avgt        0.062           s/op
QueryBenchmark.concatenatedStringApproach                                       500000          1000000  avgt        0.051           s/op
QueryBenchmark.temporaryTableNaiveApproach                                      500000            10000  avgt        4.299           s/op
QueryBenchmark.temporaryTableNaiveApproach                                      500000           100000  avgt       19.580           s/op
QueryBenchmark.temporaryTableNaiveApproach                                      500000          1000000  avgt       22.958           s/op
QueryBenchmark.temporaryTableConcatenatedStringApproach                         500000            10000  avgt        0.182           s/op
QueryBenchmark.temporaryTableConcatenatedStringApproach                         500000           100000  avgt        0.123           s/op
QueryBenchmark.temporaryTableConcatenatedStringApproach                         500000          1000000  avgt        0.111           s/op
QueryBenchmark.temporaryTableBatchedApproach                                    500000            10000  avgt        0.257           s/op
QueryBenchmark.temporaryTableBatchedApproach                                    500000           100000  avgt        0.456           s/op
QueryBenchmark.temporaryTableBatchedApproach                                    500000          1000000  avgt        0.493           s/op

Benchmark                                  (productRecommendationNumber)  (queryListSize)  Mode  Cnt  Score   Error  Units
QueryBenchmark.naiveQueryApproach                                5000000            10000  avgt       1.573           s/op
QueryBenchmark.naiveQueryApproach                                5000000           100000  avgt       1.500           s/op
QueryBenchmark.naiveQueryApproach                                5000000          1000000  avgt       1.188           s/op
QueryBenchmark.concatenatedStringApproach                        5000000            10000  avgt       1.658           s/op
QueryBenchmark.concatenatedStringApproach                        5000000           100000  avgt       1.383           s/op
QueryBenchmark.concatenatedStringApproach                        5000000          1000000  avgt       0.728           s/op

Benchmark                                                (productRecommendationNumber)  (queryListSize)  Mode  Cnt   Score   Error  Units
QueryBenchmark.temporaryTableNaiveApproach                                     5000000            10000  avgt        6.916           s/op
QueryBenchmark.temporaryTableNaiveApproach                                     5000000           100000  avgt       43.784           s/op
QueryBenchmark.temporaryTableNaiveApproach                                     5000000          1000000  avgt     too long           s/op
QueryBenchmark.temporaryTableBatchedApproach                                   5000000            10000  avgt        2.286           s/op
QueryBenchmark.temporaryTableBatchedApproach                                   5000000           100000  avgt        2.906           s/op
QueryBenchmark.temporaryTableConcatenatedStringApproach                        5000000            10000  avgt        2.185           s/op
QueryBenchmark.temporaryTableConcatenatedStringApproach                        5000000           100000  avgt        2.192           s/op
```

In those charts, the lower runtime is, the faster the approach is. And it seems like having a concatenated string of ids
wins in this case, when one column is only 4 bytes wide. However, in my experience, approach with a temporary table is 
faster when the table is wider with optional ~200 char text fields and there are concurrent reads and writes
to the main table.

### Conclusion

Anyway, it seems like the winner is `concatenatedStringApproach`, and the resulting sql query is 
`select id from product_recommendation pr where not (pr.product_id1 = any(string_to_array(?, ',')::integer[]))`,
given the argument is passed as a comma-joined string of integers. (e.g. `1,2,34,56,789`)

---

### Additional tests

In tests above, a new instance of database was created on each benchmark run, and it added its toll to how fast
tests were. I decided to manually run a database container and populate it with some data:

```sql
postgres.public> insert into product select from generate_series(1, 500000)
[2023-07-13 15:07:38] 500,000 rows affected in 865 ms
postgres.public> insert into product_recommendation (product_id1, product_id2)
                 select floor(random() * 500000 + 1)::int, floor(random() * 500000 + 1)::int
                 from generate_series(1, 5000000)
[2023-07-13 15:08:38] 5,000,000 rows affected in 59 s 641 ms
```

It took about a minute to populate the container. And then I realised I have forgotten to adjust number of iterations
from 1 when I was troubleshooting to 2 2s warmup iterations and 5 10s benchmark iterations.
Twentyish minutes later I got the following, with concatenated string being the fastest, with naive query being the second.   

```text
Benchmark                                                (queryListSize)  Mode  Cnt   Score   Error  Units
QueryBenchmark.naiveQueryApproach                                  10000  avgt    5   1.763 ± 0.132   s/op
QueryBenchmark.naiveQueryApproach                                 100000  avgt    5   1.739 ± 0.114   s/op
QueryBenchmark.concatenatedStringApproach                          10000  avgt    5   1.773 ± 0.068   s/op
QueryBenchmark.concatenatedStringApproach                         100000  avgt    5   1.626 ± 0.092   s/op
QueryBenchmark.temporaryTableNaiveApproach                         10000  avgt    5   6.515 ± 0.169   s/op
QueryBenchmark.temporaryTableNaiveApproach                        100000  avgt    5  42.482 ± 0.725   s/op
QueryBenchmark.temporaryTableConcatenatedStringApproach            10000  avgt    5   1.987 ± 0.088   s/op
QueryBenchmark.temporaryTableConcatenatedStringApproach           100000  avgt    5   2.072 ± 0.041   s/op
QueryBenchmark.temporaryTableBatchedApproach                       10000  avgt    5   2.131 ± 0.200   s/op
QueryBenchmark.temporaryTableBatchedApproach                      100000  avgt    5   2.771 ± 0.195   s/op
```