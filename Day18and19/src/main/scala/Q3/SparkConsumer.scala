package Q3

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.types._

object SparkConsumer {

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("Kafka Structured Streaming")
      .master("local[*]")
      .getOrCreate()

    spark.sparkContext.setLogLevel("WARN")

    val kafkaSource = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092") 
      .option("subscribe", "transactions") 
      .option("startingOffsets", "latest") 
      .load()

    val transactionSchema = StructType(Seq(
      StructField("transactionId", StringType, nullable = false),
      StructField("userId", StringType, nullable = false),
      StructField("amount", DoubleType, nullable = false),
    ))

    val parsedMessages = kafkaSource.selectExpr("CAST(value AS STRING) as jsonString", "timestamp")
      .select(from_json(col("jsonString"), transactionSchema).as("data"), col("timestamp"))
      .select("data.*", "timestamp")

    val windowedAggregation = parsedMessages
      .withWatermark("timestamp", "10 seconds") 
      .groupBy(
        window(col("timestamp"), "10 seconds") 
      )
      .agg(sum("amount").as("totalAmount"))

    val query = windowedAggregation.writeStream
      .outputMode("update") 
      .format("console")
      .trigger(Trigger.ProcessingTime("10 seconds"))
      .option("truncate", false) 
      .start()

    query.awaitTermination()
  }

}
