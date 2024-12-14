import org.apache.spark.sql.functions._
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.Row
import org.scalatest.FunSuite

class SparkAggregationTest extends FunSuite {

  val spark = SparkSession.builder()
    .appName("SparkAggregationTest")
    .master("local[*]")
    .getOrCreate()

  test("aggregate ratings per movie") {
    import spark.implicits._

    // Sample movie ratings data
    val movieRatings = Seq(
      (1, 4.5),  // movieId 1, rating 4.5
      (1, 3.0),  // movieId 1, rating 3.0
      (2, 5.0),  // movieId 2, rating 5.0
      (2, 4.0),  // movieId 2, rating 4.0
      (3, 2.5)   // movieId 3, rating 2.5
    )

    val movieDF = movieRatings.toDF("movieId", "rating")

    // Aggregation logic: Calculate total ratings and average rating per movie
    val aggregatedDF = movieDF
      .groupBy("movieId")
      .agg(
        count("rating").as("total_ratings"),
        avg("rating").as("average_rating")
      )

    val result = aggregatedDF.collect()

    // Validate the aggregation
    assert(result.length == 3)
    assert(result(0).getAs[Int]("total_ratings") == 2)  // Movie 1 has 2 ratings
    assert(result(0).getAs[Double]("average_rating") == 3.75)  // Average rating for Movie 1
    assert(result(1).getAs[Int]("total_ratings") == 2)  // Movie 2 has 2 ratings
    assert(result(1).getAs[Double]("average_rating") == 4.5)  // Average rating for Movie 2
    assert(result(2).getAs[Int]("total_ratings") == 1)  // Movie 3 has 1 rating
    assert(result(2).getAs[Double]("average_rating") == 2.5)  // Average rating for Movie 3
  }
}
