import org.apache.spark.sql.SparkSession

object Q1 {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("CountWords")
      .master("local[*]")
      .getOrCreate()

    val data = List("Hello everyone", "This is Rupal", "I am here to learn Spark")
    val rdd = spark.sparkContext.parallelize(data)

    val wordCount = rdd.flatMap(line => line.split(" "))
      .count()

    println(s"Word Count: $wordCount")

    spark.stop()
  }
}