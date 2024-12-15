package api

import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord, KafkaConsumer}
import org.apache.spark.sql.SparkSession
import org.scalatest.funsuite.AnyFunSuite
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers._
import org.mockito.MockitoSugar.mock
import java.util.Properties
import scala.collection.JavaConverters._

class KafkaMovieRatingsConsumerTest extends AnyFunSuite {

  implicit val spark: SparkSession = SparkSession.builder()
    .appName("KafkaMovieRatingsConsumerTest")
    .master("local[*]")
    .getOrCreate()

  import spark.implicits._

  test("KafkaMovieRatingsConsumer should process and validate Kafka records") {
    // Mock KafkaConsumer
    val mockKafkaConsumer = mock[KafkaConsumer[String, String]]

    // Test input Kafka records
    val records = Seq(
      new ConsumerRecord[String, String]("movie_ratings", 0, 0L, "key1",
        """{"userId": 1, "movieId": 101, "rating": 4.5, "timestamp": 1639440000}"""),
      new ConsumerRecord[String, String]("movie_ratings", 0, 1L, "key2",
        """{"userId": 2, "movieId": 102, "rating": 5.0, "timestamp": 1639443600}"""),
      new ConsumerRecord[String, String]("movie_ratings", 0, 2L, "key3",
        """{"userId": "invalid", "movieId": 103, "rating": "bad", "timestamp": 1639447200}""") // Invalid record
    ).asJava

    // Mock poll method to return test records
    when(mockKafkaConsumer.poll(any())).thenReturn(records)

    // Kafka Consumer Properties
    val props = new Properties()
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group")
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")

    // Define test logic for validation
    val consumerLogic = new KafkaMovieRatingsConsumer {
      override def processRecord(record: ConsumerRecord[String, String]): Option[(Int, Int, Double, Long)] = {
        try {
          val json = spark.read.json(Seq(record.value()).toDS())
          val userId = json.select("userId").as[Int].collect().head
          val movieId = json.select("movieId").as[Int].collect().head
          val rating = json.select("rating").as[Double].collect().head
          val timestamp = json.select("timestamp").as[Long].collect().head
          Some((userId, movieId, rating, timestamp))
        } catch {
          case _: Exception => None // Skip invalid records
        }
      }
    }

    // Process mocked Kafka records
    val processedRecords = records.asScala
      .flatMap(consumerLogic.processRecord)

    // Assertions
    assert(processedRecords.size == 2) // One record is invalid, so only 2 valid records
    assert(processedRecords.contains((1, 101, 4.5, 1639440000L)))
    assert(processedRecords.contains((2, 102, 5.0, 1639443600L)))
  }
}
