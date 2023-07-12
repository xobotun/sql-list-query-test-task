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

### Conclusion