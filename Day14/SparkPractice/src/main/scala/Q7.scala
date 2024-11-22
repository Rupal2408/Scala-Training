import org.apache.spark.sql.SparkSession

object Q7 {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("UnionRDDs")
      .master("local[*]")
      .getOrCreate()

    val rdd1 = spark.sparkContext.parallelize(List(1, 2, 3, 4, 5))
    val rdd2 = spark.sparkContext.parallelize(List(4, 5, 6, 7, 8))

    val unionRdd = rdd1.union(rdd2).distinct()

    unionRdd.collect().foreach(println)

    spark.stop()
  }
}
