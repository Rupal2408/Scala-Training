/*
Exercise 2: Narrow vs Wide Transformations
Objective: Differentiate between narrow and wide transformations in Spark.
Task:

Create an RDD of numbers from 1 to 1000.
Apply narrow transformations: map, filter.
Apply a wide transformation: groupByKey or reduceByKey (simulate by mapping numbers into key-value pairs, e.g., (number % 10, number)).
Save the results to a text file.
Expected Analysis:

Identify how narrow transformations execute within a single partition, while wide transformations cause shuffles.
Observe the DAG in the Spark UI, focusing on stages and shuffle operations.
 */

import org.apache.spark.sql.SparkSession

object Exercise2 {
  def main(args: Array[String]): Unit = {

    // Initialize the Spark session
    val spark = SparkSession.builder()
      .appName("Narrow vs Wide Transformations")
      .master("local[2]")  // Local mode with 2 threads
      .getOrCreate()

    val sc = spark.sparkContext

    // Create an RDD with numbers from 1 to 1000
    val rdd = sc.parallelize(1 to 1000)  // Split into 4 partitions

    // Narrow transformation: map
    val mappedRdd = rdd.map(x => x * 2)

    // Narrow transformation: filter
    val filteredRdd = mappedRdd.filter(x => x % 2 == 0)

    // Wide transformation: groupByKey
    val keyValueRdd = rdd.map(x => (x % 10, x))  // Map numbers into key-value pairs (key = number % 10)
    val groupedRdd = keyValueRdd.groupByKey()  // Group the numbers by the key (mod 10)

    // Save the narrow transformation result (filtered numbers) to a text file
    filteredRdd.saveAsTextFile("output/narrow_transformation")

    // Save the wide transformation result (grouped by key) to a text file
    groupedRdd.saveAsTextFile("output/wide_transformation")
    // Hold the Spark UI
    println("Application is running. Press Enter to exit.")
    scala.io.StdIn.readLine()
    // Stop the Spark session
    spark.stop()
  }
}