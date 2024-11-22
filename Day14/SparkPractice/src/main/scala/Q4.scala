import org.apache.spark.sql.SparkSession

object Q4 {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("CharacterFrequency")
      .master("local[*]")
      .getOrCreate()

    val data = List("spark", "rdd", "scala", "project", "tasks")
    val rdd = spark.sparkContext.parallelize(data)

    val characterCount = rdd.flatMap(line => line)
      .map(char => (char, 1))
      .reduceByKey(_ + _)

    characterCount.collect().foreach(println)

    spark.stop()
  }
}
