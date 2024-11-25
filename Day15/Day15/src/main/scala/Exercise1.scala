/*
Exercise 1: Understanding RDD and Partitioning
Objective: Create and manipulate an RDD while understanding its partitions.
Task:

Load a large text file (or create one programmatically with millions of random numbers).
Perform the following:
Check the number of partitions for the RDD.
Repartition the RDD into 4 partitions and analyze how the data is distributed.
Coalesce the RDD back into 2 partitions.
Print the first 5 elements from each partition.
Expected Analysis:

View the effect of repartition and coalesce in the Spark UI (stages, tasks).
 */

import org.apache.spark.sql.SparkSession

import scala.util.Random

object Exercise1 {
  def main(args: Array[String]): Unit = {

    // Create a SparkSession
    val spark = SparkSession.builder()
      .appName("RDD Partitioning Example")
      .master("local[2]")
      .getOrCreate()

    val sc = spark.sparkContext

    // Create an RDD with 1 million random numbers
    val rdd = sc.parallelize((1 to 1000000).map(_ => Random.nextInt(1000)))

    // Print the initial number of partitions
    println(s"Initial number of partitions: ${rdd.getNumPartitions}")

    // Repartition the RDD into 4 partitions
    val rddRepartitioned = rdd.repartition(4)
    println(s"Number of partitions after repartitioning: ${rddRepartitioned.getNumPartitions}")

    // Coalesce the RDD back into 2 partitions
    val rddCoalesced = rddRepartitioned.coalesce(2)
    println(s"Number of partitions after coalescing: ${rddCoalesced.getNumPartitions}")

    // Print the first 5 elements from each partition
    rddCoalesced.mapPartitionsWithIndex((index, iterator) => {
      Iterator(s"Partition $index: ${iterator.take(5).mkString(", ")}")
    }).collect().foreach(println)

    // Hold the Spark UI
    println("Application is running. Press Enter to exit.")
    scala.io.StdIn.readLine()

    // Stop the Spark session
    spark.stop()
  }
}

/*
Output:
Initial number of partitions: 2
Number of partitions after repartitioning: 4
Number of partitions after coalescing: 2
Partition 0: 267, 833, 756, 953, 532
Partition 1: 29, 647, 308, 405, 658
 */