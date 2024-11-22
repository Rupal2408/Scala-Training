import org.apache.spark.sql.SparkSession

object Q3 {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("FilterOddNumbers")
      .master("local[*]")
      .getOrCreate()

    val rdd = spark.sparkContext.parallelize(List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))

    val filterRdd = rdd.filter(x => x % 2 != 0)

    filterRdd.collect().foreach(println)

    spark.stop()
  }
}
