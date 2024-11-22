import org.apache.spark.sql.SparkSession

object Q10 {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("GroupByKeyAndComputeSum")
      .master("local[*]")
      .getOrCreate()

    val data = List((1, 10), (1, 20), (2, 30), (2, 40), (3, 50))
    val rdd = spark.sparkContext.parallelize(data)

    val groupRdd = rdd.groupByKey()

    val sumRdd = groupRdd.mapValues(values => values.sum)

    sumRdd.collect().foreach(println)

    spark.stop()
  }
}
