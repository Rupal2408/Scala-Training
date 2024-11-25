/*
Exercise 4: Exploring DAG and Spark UI
Objective: Analyze the DAG and understand the stages involved in a complex Spark job.
Task:

Create an RDD of integers from 1 to 10,000.
Perform a series of transformations:

Filter: Keep only even numbers.
Map: Multiply each number by 10.
Map: Generate a tuple where the first element is the remainder when dividing the number by 100 (key), and the second is the number itself (value).
ReduceByKey: Group by the remainder (key) and compute the sum of the values.
Finally, perform an action to collect the results and display them.
Expected Analysis:

Analyze the DAG generated for the job and how Spark breaks it into stages.
Compare execution times of stages and tasks in the Spark UI.
 */

import org.apache.spark.sql.SparkSession

object Exercise4 {
  def main(args: Array[String]): Unit = {

    // Initialize Spark session
    val spark = SparkSession.builder()
      .appName("DAG Analysis")
      .master("local[2]")
      .getOrCreate()

    val sc = spark.sparkContext

    // Create an RDD of integers from 1 to 10,000
    val rdd = sc.parallelize(1 to 10000)

    // Apply transformations
    val filteredRdd = rdd.filter(_ % 2 == 0)  // Filter even numbers

    val mappedRdd1 = filteredRdd.map(_ * 10)  // Multiply each number by 10

    val mappedRdd2 = mappedRdd1.map(num => (num % 100, num))  // Map to (key, value) where key is num % 100

    val reducedRdd = mappedRdd2.reduceByKey(_ + _)  // Reduce by key to sum the values for each key

    // Trigger an action to collect the result and display them
    val result = reducedRdd.collect()

    result.foreach(println)

    // Hold the Spark UI
    println("Application is running. Press Enter to exit.")
    scala.io.StdIn.readLine()
    // Stop the Spark session
    spark.stop()
  }
}