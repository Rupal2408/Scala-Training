package consumer

import aggregator.{GenreMetricsAggregator, MovieMetricsAggregator, UserDemoMetricsAggregator}
import org.apache.spark.sql.{Dataset, Row, SaveMode, SparkSession}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.types._

import java.util.concurrent.TimeUnit
import config.Configuration

object KafkaMovieRatingsConsumer {
  def main(args: Array[String]): Unit = {
    val KAFKA_BOOTSTRAP_SERVERS: String = Configuration.kafkaBootstrapServers
    val spark = SparkSession.builder()
      .appName("MovieRatingsConsumer")
      .config("spark.hadoop.fs.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFileSystem")
      .config("spark.hadoop.fs.AbstractFileSystem.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFS")
      .config("spark.hadoop.google.cloud.auth.service.account.enable", "true")
      .config("spark.hadoop.google.cloud.auth.service.account.json.keyfile", Configuration.gcsJsonKeyfile)
      .master("local[*]")
      .getOrCreate()

    val ratingsSchema = new StructType()
      .add("userId", IntegerType)
      .add("movieId", IntegerType)
      .add("rating", FloatType)
      .add("timestamp", StringType)


    val ratingsStream = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", KAFKA_BOOTSTRAP_SERVERS)
      .option("subscribe", Configuration.kafkaTopic)
      .option("startingOffsets", "latest")
      .load()
      .selectExpr("CAST(value AS STRING) as jsonString")
      .select(from_json(col("jsonString"), ratingsSchema).as("rating"))
      .select("rating.*")

    ratingsStream.writeStream.foreachBatch { (batchDF: Dataset[Row], batchId: Long) =>
      val moviesDF = spark.read.option("header", "true").csv(Configuration.moviesDataPath)
      val usersDF = spark.read.option("header", "true").csv(Configuration.usersDataPath)

      val filteredStream = batchDF.filter(col("rating").between(0.5, 5.0))

      val enrichedDF = filteredStream
        .join(broadcast(moviesDF), "movieId")
        .join(broadcast(usersDF), "userId")
        .withColumn("date", to_date(from_unixtime(col("timestamp") / 1000)))

      enrichedDF
        .write
        .mode(SaveMode.Append)
        .partitionBy("date")
        .parquet(Configuration.enrichedDataPath)

      println("Saved Enriched Data Successfully.")

      new MovieMetricsAggregator().aggregate(enrichedDF)
      new GenreMetricsAggregator().aggregate(enrichedDF)
      new UserDemoMetricsAggregator().aggregate(enrichedDF)

    }.trigger(Trigger.ProcessingTime(100, TimeUnit.SECONDS)) // processing the data every 60 seconds
      .start().awaitTermination()

    println("Completed All Aggregation Process successfully.")
    spark.stop()
  }
}

