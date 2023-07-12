package com.xobotun;

/**
 * Task:
 *
 * In an SQL database, there is a table called product_recommendation with columns id, product_id1, and product_id2.
 * The id column is the primary key, and the other two columns are foreign keys referencing the product table
 * (the specific structure of which is not relevant for this task). All columns are of type integer.
 * The product_recommendation table contains several million records.
 *
 * In a Java application, there is an array or any collection (of your choice) containing 100,000 product IDs
 * (also integers). The goal is to obtain a list of all recommendations (rows from the product_recommendation table)
 * where product_id1 is NOT in this array/collection.
 *
 * Outline the main steps for solving this task and provide the main SQL query that needs to be executed in the process.
 */