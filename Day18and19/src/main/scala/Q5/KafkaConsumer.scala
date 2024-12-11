package Q5

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{broadcast, col, from_json}
import org.apache.spark.sql.types._

object KafkaConsumer {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("Generate Enriched Data")
      .config("spark.hadoop.fs.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFileSystem")
      .config("spark.hadoop.fs.AbstractFileSystem.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFS")
      .config("spark.hadoop.google.cloud.auth.service.account.enable", "true")
      .config("spark.hadoop.google.cloud.auth.service.account.json.keyfile", "/Users/rupalgupta/gcp-final-key.json")
      .master("local[*]")
      .getOrCreate()

    val userDetailsPath = "gs://scala_assgn_bucket/Day18and19Tasks/user_details.csv"
    val outputPath = "gs://scala_assgn_bucket/Day18and19Tasks/enriched_orders/"

    spark.sparkContext.setLogLevel("WARN")

    val kafkaBootstrapServers = "localhost:9092"
    val kafkaTopic = "orders"

    val kafkaSchema = new StructType()
      .add("orderId", StringType)
      .add("userId", StringType)
      .add("amount", DoubleType)

    val userDetailsDF = spark.read
      .option("header", "true")
      .option("inferSchema", "true")
      .csv(userDetailsPath)
      .cache() 

    val kafkaStreamDF = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", kafkaBootstrapServers)
      .option("subscribe", kafkaTopic)
      .option("startingOffsets", "latest")
      .load()

    val parsedStreamDF = kafkaStreamDF
      .selectExpr("CAST(value AS STRING) as jsonString")
      .select(from_json(col("jsonString"), kafkaSchema).as("data"))
      .select("data.*") 

    val enrichedStreamDF = parsedStreamDF.join(broadcast(userDetailsDF), Seq("userId"), "left_outer").drop(userDetailsDF("userId")) // Avoid duplicate columns

    val query = enrichedStreamDF.writeStream
      .outputMode("append")
      .format("json")
      .option("path", outputPath)
      .option("checkpointLocation", "/tmp/spark-kafka-enrichment-checkpoints") 
      .start()

    query.awaitTermination()
  }
}