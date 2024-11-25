/*
Exercise 5: Partitioning Impact on Performance
Objective: Understand the impact of partitioning on performance and data shuffling.
Task:

Load a large dataset (e.g., a CSV or JSON file) into an RDD.
Partition the RDD into 2, 4, and 8 partitions separately and perform the following tasks:
Count the number of rows in the RDD.
Sort the data using a wide transformation.
Write the output back to disk.
Compare execution times for different partition sizes.
Expected Analysis:

Observe how partition sizes affect shuffle size and task distribution in the Spark UI.
Understand the trade-off between too many and too few partitions.
*/

import org.apache.spark.sql.SparkSession

object Exercise5 {
  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
      .appName("Partitioning Impact on Performance")
      .master("local[4]")  // Local mode with 4 threads
      .getOrCreate()

    val sc = spark.sparkContext

    val data = sc.textFile("src/main/scala/LargeDataSet.csv")

    def runTasksWithPartitionSize(partitions: Int): Unit = {
      println(s"Running with $partitions partitions")

      val partitionedRdd = data.repartition(partitions)

      val count = partitionedRdd.count()
      println(s"Number of rows: $count")

      val sortedRdd = partitionedRdd.sortBy(line => line)

      val outputPath = s"output_partition_$partitions"
      sortedRdd.saveAsTextFile(outputPath)

      val startTime = System.nanoTime()
      sortedRdd.collect()
      val endTime = System.nanoTime()

      val executionTime = (endTime - startTime) / 1e9d
      println(s"Execution time with $partitions partitions: $executionTime seconds")
    }

    runTasksWithPartitionSize(2)
    runTasksWithPartitionSize(4)
    runTasksWithPartitionSize(8)

    println("Press Enter to exit the application.")
    scala.io.StdIn.readLine()
    spark.stop()
  }
}

/*
Output:
Running with 2 partitions
Number of rows: 100001
Execution time with 2 partitions: 0.17651975 seconds
Running with 4 partitions
Number of rows: 100001
Execution time with 4 partitions: 0.059835417 seconds
Running with 8 partitions
Number of rows: 100001
Execution time with 8 partitions: 0.071816334 seconds
 */