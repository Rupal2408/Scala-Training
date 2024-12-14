import org.apache.spark.sql.functions._
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types._
import org.apache.spark.sql.Row
import org.scalatest.FunSuite

class SparkStreamValidationTest extends FunSuite {

  val spark = SparkSession.builder()
    .appName("SparkStreamValidationTest")
    .master("local[*]")
    .getOrCreate()

  test("validate rating range between 0.5 and 5.0") {
    import spark.implicits._

    // Test data
    val testData = Seq(
      Row(1, 1, 4.5),  // Valid rating
      Row(2, 2, 0.3),  // Invalid rating
      Row(3, 3, 5.1)   // Invalid rating
    )

    val schema = StructType(Seq(
      StructField("userId", IntegerType),
      StructField("movieId", IntegerType),
      StructField("rating", DoubleType)
    ))

    val testDF = spark.createDataFrame(spark.sparkContext.parallelize(testData), schema)

    // Data validation logic
    val validRatingsDF = testDF.filter($"rating" >= 0.5 && $"rating" <= 5.0)

    // Assert that invalid rows are excluded
    assert(validRatingsDF.count() == 1)
  }
}
