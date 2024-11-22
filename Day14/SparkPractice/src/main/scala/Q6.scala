import org.apache.spark.sql.SparkSession

object Q6 {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("JoinRDDs")
      .master("local[*]")
      .getOrCreate()

    val names = List((1, "Ross"), (2, "Monica"), (3, "Chandler"))
    val scores = List((1, 85), (2, 93), (3, 74))

    val namesRdd = spark.sparkContext.parallelize(names)
    val scoresRdd = spark.sparkContext.parallelize(scores)

    namesRdd.join(scoresRdd).map{
        case (id, (name, score)) => (id, name, score)
      }.collect().foreach(println)

    spark.stop()
  }
}
