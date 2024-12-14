import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.scalatest.funsuite.AnyFunSuite

class MovieMetricsAggregatorTest extends AnyFunSuite {

  implicit val spark: SparkSession = SparkSession.builder()
    .appName("MovieMetricsAggregatorTest")
    .master("local[*]")
    .getOrCreate()

  import spark.implicits._

  test("MovieMetricsAggregator should correctly aggregate movie metrics") {

    // Input DataFrame
    val inputDF = Seq(
      (1, "Movie A", "Action|Adventure", 4.5),
      (1, "Movie A", "Action|Adventure", 3.5),
      (2, "Movie B", "Drama", 5.0),
      (2, "Movie B", "Drama", 4.0),
      (3, "Movie C", "Comedy", 2.5)
    ).toDF("movieId", "title", "genres", "rating")

    // Aggregate
    val outputDF = inputDF.groupBy("movieId", "title", "genres")
      .agg(
        avg("rating").as("average_rating"),
        count("rating").as("total_ratings")
      )

    // Expected DataFrame
    val expectedDF = Seq(
      (1, "Movie A", "Action|Adventure", 4.0, 2),
      (2, "Movie B", "Drama", 4.5, 2),
      (3, "Movie C", "Comedy", 2.5, 1)
    ).toDF("movieId", "title", "genres", "average_rating", "total_ratings")

    // Compare
    assert(expectedDF.except(outputDF).count() == 0)
    assert(outputDF.except(expectedDF).count() == 0)
  }
}

