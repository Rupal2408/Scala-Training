import aggregator.{GenreMetricsAggregator, MovieMetricsAggregator, UserDemoMetricsAggregator}
import org.apache.spark.sql.{Dataset, Row, SparkSession}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.scalatest.{BeforeAndAfterAll}
import org.scalatest.funsuite.AnyFunSuite

class ConsumerValidationTest extends AnyFunSuite with BeforeAndAfterAll {

  var spark: SparkSession = _

  override def beforeAll(): Unit = {
    super.beforeAll()
    spark = SparkSession.builder()
      .appName("KafkaMovieRatingsConsumerTest")
      .master("local[*]")
      .getOrCreate()
  }

  override def afterAll(): Unit = {
    if (spark != null) {
      spark.stop()
    }
    super.afterAll()
  }

  test("Validate schema and filtering logic") {
    val ratingsSchema = new StructType()
      .add("userId", IntegerType)
      .add("movieId", IntegerType)
      .add("rating", FloatType)
      .add("timestamp", StringType)

    val ratingsData = Seq(
      (1, 101, 4.0f, "1633027200000"),
      (2, 102, 5.0f, "1633027200000"),
      (3, 103, 2.5f, "1633027200000"),
      (4, 104, 6.0f, "1633027200000"), // Invalid rating > 5.0
      (5, 105, 0.3f, "1633027200000")  // Invalid rating < 0.5
    )

    val ratingsDF = spark.createDataFrame(ratingsData).toDF("userId", "movieId", "rating", "timestamp")

    // Filter for valid ratings
    val filteredDF = ratingsDF.filter(col("rating").between(0.5, 5.0))

    assert(filteredDF.count() == 3)
    assert(filteredDF.filter(col("rating") > 5.0 || col("rating") < 0.5).count() == 0)
  }

  test("Validate enrichment logic") {
    val ratingsData = Seq(
      (1, 101, 4.0f, "1633027200000"),
      (2, 102, 5.0f, "1633027200000")
    )

    val moviesData = Seq(
      (101, "Movie A", "Action"),
      (102, "Movie B", "Drama")
    )

    val usersData = Seq(
      (1, 25, "Male", "New York"),
      (2, 30, "Female", "Los Angeles")
    )

    val ratingsDF = spark.createDataFrame(ratingsData).toDF("userId", "movieId", "rating", "timestamp")
    val moviesDF = spark.createDataFrame(moviesData).toDF("movieId", "title", "genres")
    val usersDF = spark.createDataFrame(usersData).toDF("userId", "age", "gender", "location")

    // Perform enrichment
    val enrichedDF = ratingsDF
      .join(broadcast(moviesDF), "movieId")
      .join(broadcast(usersDF), "userId")
      .withColumn("date", to_date(from_unixtime(col("timestamp") / 1000)))

    enrichedDF.show()

    // Validate results
    assert(enrichedDF.count() == 2)
    assert(enrichedDF.columns.contains("title"))
    assert(enrichedDF.columns.contains("genres"))
    assert(enrichedDF.columns.contains("age"))
    assert(enrichedDF.columns.contains("gender"))
    assert(enrichedDF.columns.contains("location"))
    assert(enrichedDF.columns.contains("date"))

    val expectedData = Seq(
      (1, 101, 4.0f, "1633027200000", "Movie A", "Action", 25, "Male", "New York", "2021-10-01"),
      (2, 102, 5.0f, "1633027200000", "Movie B", "Drama", 30, "Female", "Los Angeles", "2021-10-01")
    )

    val expectedDF = spark.createDataFrame(expectedData)
      .toDF("userId", "movieId", "rating", "timestamp", "title", "genres", "age", "gender", "location", "date")

    assert(enrichedDF.except(expectedDF).count() == 0)
    assert(expectedDF.except(enrichedDF).count() == 0)
  }


}
