import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.scalatest.funsuite.AnyFunSuite

class GenreMetricsAggregatorTest extends AnyFunSuite {
  val spark: SparkSession = SparkSession.builder()
    .appName("GenreMetricsAggregatorTest")
    .master("local[*]")
    .getOrCreate()

  import spark.implicits._

  test("GenreMetricsAggregator should correctly aggregate data") {
    val inputDF = Seq(
      ("Movie1", "Comedy|Drama", 4.5),
      ("Movie2", "Action|Comedy", 3.0),
      ("Movie3", "Drama", 5.0)
    ).toDF("title", "genres", "rating")

    val expectedDF = Seq(
      ("Comedy", 3.75, 2),
      ("Drama", 4.75, 2),
      ("Action", 3.0, 1)
    ).toDF("genre", "average_rating", "total_ratings")

    val aggregatedDF = inputDF
      .withColumn("genre", explode(split(col("genres"), "\\|")))
      .groupBy("genre")
      .agg(
        avg("rating").as("average_rating"),
        count("rating").as("total_ratings")
      )

    assert(expectedDF.except(aggregatedDF).count() == 0)
    assert(aggregatedDF.except(expectedDF).count() == 0)
  }
}
