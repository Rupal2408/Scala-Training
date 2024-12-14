import org.apache.spark.sql.{DataFrame, Dataset, Encoders, Row, SparkSession}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.streaming.StreamingQuery
import org.apache.spark.storage.StorageLevel


object KafkaConsumer {
  def main(args: Array[String]): Unit = {
    // Create Spark session
    val spark = SparkSession.builder()
      .appName("WalmartSalesConsumer")
      .config("spark.hadoop.fs.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFileSystem")
      .config("spark.hadoop.fs.AbstractFileSystem.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFS")
      .config("spark.hadoop.google.cloud.auth.service.account.enable", "true")
      .config("spark.hadoop.google.cloud.auth.service.account.json.keyfile", "/Users/rupalgupta/gcp-final-key.json")
      .master("local[*]")
      .getOrCreate()

    // Initialize the SparkDataProcessor
    val processor = new SparkDataProcessor()
    val config = new Configuration()

    println("kafka consumer")

   val (featuresDF, storesDF) = processor.loadStaticData(spark, config)

    // Define Kafka parameters
    val kafkaBootstrapServers = config.kafkaBootstrapServers
    val kafkaTopic = config.kafkaTopic

    // Read sales data stream from Kafka
    val salesStream = processor.cleanSalesData(readSalesData(spark, kafkaBootstrapServers, kafkaTopic))

    // Process the sales data stream and update metrics
    processStream(salesStream, featuresDF, storesDF, config)

  }

  def readSalesData(spark: SparkSession, kafkaBootstrapServers: String, kafkaTopic: String): DataFrame = {
    // Read streaming data from Kafka
    val salesStream = spark.readStream
      .format("kafka") // Specify Kafka as the data source
      .option("kafka.bootstrap.servers", kafkaBootstrapServers)
      .option("subscribe", kafkaTopic)
      .option("startingOffsets", "latest")
      .load()
      .selectExpr("CAST(value AS STRING)") // Get the message body as a string

    // Define the schema for the SalesData case class
    val salesSchema = Encoders.product[SalesData].schema

    val salesDataDf = salesStream
      .select(from_json(col("value"), salesSchema).as("sales"))
      .select("sales.*")

    salesDataDf
  }

  def processStream(salesStream: DataFrame, featuresDF: DataFrame, storesDF :DataFrame, config:Configuration): Unit = {
    val query = salesStream.writeStream
      .foreachBatch { (batchDf: Dataset[Row], _: Long) =>
        val persistedBatchDf = batchDf.persist(StorageLevel.MEMORY_AND_DISK)

        val enrichedSalesDf = persistedBatchDf.join(featuresDF, Seq("Store", "Date"))
          .join(storesDF, "Store")
          .repartition(col("Store"), col("Date"))

        storeMetrics(enrichedSalesDf, config)
        departmentMetrics(enrichedSalesDf, config)

        enrichedSalesDf.write
          .mode("append")
          .format("parquet")  // Display output in the parquet
          .partitionBy("Store", "Date")
          .parquet(config.enrichedDataOutputPath)
        enrichedSalesDf.unpersist()
        persistedBatchDf.unpersist()
        ()
      }
      .trigger(Trigger.ProcessingTime("30 seconds"))
      .start()

    query.awaitTermination()
  }

  def storeMetrics(enrichedSalesDf: DataFrame, config:Configuration): Unit = {
    val storeSalesDf = enrichedSalesDf.groupBy("Store")
      .agg(
        sum("Weekly_Sales").alias("Total_Store_Sales"),
        avg("Weekly_Sales").alias("Average_Store_Sales")
      ).orderBy(desc("Total_Store_Sales")).cache()

    println("Store Sales Metrics:")

     storeSalesDf.write
       .mode("overwrite")
       .json(config.storeMetricsOutputPath)
    storeSalesDf.unpersist()
  }

  def departmentMetrics(enrichedSalesDf: DataFrame, config: Configuration): Unit = {
    val departmentSalesDf = enrichedSalesDf.groupBy("Store","Dept")
      .agg(
        sum("Weekly_Sales").alias("Total_Dept_Sales"),
        avg("Weekly_Sales").alias("Average_Dept_Sales"),
        collect_list("Weekly_Sales").as("Weekly_Sales_Trend")
      ).cache()

    println("Department Sales Metrics:")

     departmentSalesDf.write
       .mode("overwrite")
       .json(config.deptMetricsOutputPath)

    departmentSalesDf.unpersist()
    println("enriched sales data:")

    val holidayVsNonHolidaySalesDf = enrichedSalesDf.groupBy("Store", "Dept")
      .agg(
        sum(when(col("IsHoliday") === "TRUE", col("Weekly_Sales")).otherwise(0)).alias("Total_Sales_Holiday"),
        sum(when(col("IsHoliday") === "FALSE", col("Weekly_Sales")).otherwise(0)).alias("Total_Sales_Non_Holiday"),
        avg(when(col("IsHoliday") === "TRUE", col("Weekly_Sales")).otherwise(0)).alias("Average_Sales_Holiday"),
        avg(when(col("IsHoliday") === "FALSE", col("Weekly_Sales")).otherwise(0)).alias("Average_Sales_Non_Holiday")
      )

    holidayVsNonHolidaySalesDf.cache()

    println("Holiday vs Non-Holiday Sales Metrics:")

      holidayVsNonHolidaySalesDf.write
        .mode("overwrite")
        .json(config.deptIsHolidayOutputPath)
    holidayVsNonHolidaySalesDf.unpersist()
  }
  }
