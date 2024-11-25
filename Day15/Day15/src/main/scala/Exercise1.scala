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

    val spark = SparkSession.builder()
      .appName("RDD Partitioning Example")
      .master("local[2]")
      .getOrCreate()

    val sc = spark.sparkContext

    val rdd = sc.parallelize((1 to 1000000).map(_ => Random.nextInt(1000)))

    println(s"Initial number of partitions: ${rdd.getNumPartitions}")

    val rddRepartitioned = rdd.repartition(4)
    println(s"Number of partitions after repartitioning: ${rddRepartitioned.getNumPartitions}")

    val repartitionedData = rddRepartitioned.glom().collect()
    for ((partition, index) <- repartitionedData.zipWithIndex) {
      println(s"Partition $index size after repartitioning: ${partition.length}")
    }

    val rddCoalesced = rddRepartitioned.coalesce(2)
    println(s"Number of partitions after coalescing: ${rddCoalesced.getNumPartitions}")

    rddCoalesced.mapPartitionsWithIndex((index, iterator) => {
      Iterator(s"Partition $index: ${iterator.take(5).mkString(", ")}")
    }).collect().foreach(println)

    println("Press Enter to exit the application.")
    scala.io.StdIn.readLine()

    spark.stop()
  }
}

/*
Output:
Initial number of partitions: 2
Number of partitions after repartitioning: 4
Partition 0 size after repartitioning: 250000
Partition 1 size after repartitioning: 250000
Partition 2 size after repartitioning: 250000
Partition 3 size after repartitioning: 250000
Number of partitions after coalescing: 2
Partition 0: 233, 21, 505, 953, 567
Partition 1: 617, 201, 876, 923, 549
*/