import org.apache.spark.sql.functions._
import org.apache.spark.sql.{SparkSession, StreamingQuery}
import org.apache.spark.sql.streaming.Trigger
import org.scalatest.FunSuite

class SparkStreamingBatchIntegrationTest extends FunSuite {

  val spark = SparkSession.builder()
    .appName("SparkStreamingBatchIntegrationTest")
    .master("local[*]")
    .getOrCreate()

  test("streaming data to batch aggregation") {
    // Simulate streaming data
    val streamingData = spark.readStream
      .format("rate")
      .option("rowsPerSecond", 1)
      .load()

    val aggregatedDF = streamingData
      .groupBy(window(col("timestamp"), "10 seconds"))
      .agg(count("value").as("count"))

    // Define the query to write the results
    val query: StreamingQuery = aggregatedDF.writeStream
      .outputMode("update")
      .format("memory")
      .queryName("aggregated_data")
      .start()

    // Wait for the query to process some data
    query.processAllAvailable()

    // Retrieve the result from the in-memory table
    val resultDF = spark.sql("SELECT * FROM aggregated_data")

    // Assert that the aggregation happened correctly
    assert(resultDF.count() > 0)

    query.stop()
  }
}
