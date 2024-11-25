/*
Exercise 3: Analyzing Tasks and Executors
Objective: Understand how tasks are distributed across executors in local mode.
Task:

Create an RDD of strings with at least 1 million lines (e.g., lorem ipsum or repetitive text).
Perform a transformation pipeline:
Split each string into words.
Map each word to (word, 1).
Reduce by key to count word occurrences.
Set spark.executor.instances to 2 and observe task distribution in the Spark UI.
Expected Analysis:

Compare task execution times across partitions and stages in the UI.
Understand executor and task allocation for a local mode Spark job.
 */

import org.apache.spark.sql.SparkSession

object Exercise3 {
  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
      .appName("Word Count Analysis")
      .master("local[2]")  // Running Spark locally with 2 executors (local mode)
      .config("spark.executor.instances", "2")  // Explicitly setting the number of executors
      .getOrCreate()

    val sc = spark.sparkContext

    val loremIpsumText = """
      Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.
      Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.
      Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.
      Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.
    """ * 25000

    val rdd = sc.parallelize(loremIpsumText.split("\n"))

    val wordsRdd = rdd.flatMap(line => line.split(" "))

    val wordPairsRdd = wordsRdd.map(word => (word.toLowerCase, 1))

    val wordCountRdd = wordPairsRdd.reduceByKey(_ + _)

    wordCountRdd.collect()
    println("Press Enter to exit the application.")
    scala.io.StdIn.readLine()
    spark.stop()
  }
}