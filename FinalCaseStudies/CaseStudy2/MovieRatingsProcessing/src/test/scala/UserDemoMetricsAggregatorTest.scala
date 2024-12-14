
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.scalatest.funsuite.AnyFunSuite

class UserDemoMetricsAggregatorTest extends AnyFunSuite {

  implicit val spark: SparkSession = SparkSession.builder()
    .appName("UserDemoMetricsAggregatorTest")
    .master("local[*]")
    .getOrCreate()

  import spark.implicits._

  test("UserDemoMetricsAggregator should correctly aggregate user demographics metrics") {

    // Input DataFrame
    val inputDF = Seq(
      ("18-24", "M", "USA", 4.0),
      ("18-24", "M", "USA", 3.5),
      ("25-34", "F", "Canada", 5.0),
      ("25-34", "F", "Canada", 4.5),
      ("18-24", "F", "USA", 3.0)
    ).toDF("age", "gender", "location", "rating")

    // Aggregate
    val outputDF = inputDF.groupBy("age","gender","location")
      .agg(
        avg("rating").as("average_rating"),
        count("rating").as("total_ratings")
      )

    // Expected DataFrame
    val expectedDF = Seq(
      ("18-24", "M", "USA", 3.75, 2),
      ("25-34", "F", "Canada", 4.75, 2),
      ("18-24", "F", "USA", 3.0, 1)
    ).toDF("age", "gender", "location", "average_rating", "total_ratings")

    // Compare
    assert(outputDF.except(expectedDF).count() == 0)
    assert(expectedDF.except(outputDF).count() == 0)
  }
}
