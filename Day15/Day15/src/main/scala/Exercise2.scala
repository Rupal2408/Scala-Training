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

    val spark = SparkSession.builder()
      .appName("Narrow vs Wide Transformations")
      .master("local[2]")  // Local mode with 2 threads
      .getOrCreate()

    val sc = spark.sparkContext

    val rdd = sc.parallelize(1 to 1000)

    val mappedRdd = rdd.map(x => x * 2)

    val filteredRdd = mappedRdd.filter(x => x % 2 == 0)

    val keyValueRdd = rdd.map(x => (x % 10, x))
    val groupedRdd = keyValueRdd.groupByKey()

    filteredRdd.saveAsTextFile("output/narrow_transformation")

    groupedRdd.saveAsTextFile("output/wide_transformation")
    println("Press Enter to exit the application.")
    scala.io.StdIn.readLine()
    spark.stop()
  }
}