import org.apache.spark.sql.SparkSession

object Q5 {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("AverageScore")
      .master("local[*]")
      .getOrCreate()

    val scores = List((1, 80), (2, 90), (3, 70), (4, 60), (5, 85))
    val rdd = spark.sparkContext.parallelize(scores)

    val totalScore = rdd.map(_._2).reduce(_ + _)
    val totalRecords = rdd.count()

    val averageScore = totalScore.toDouble / totalRecords

    println(s"Average Score of all records: $averageScore")


    spark.stop()
  }
}
