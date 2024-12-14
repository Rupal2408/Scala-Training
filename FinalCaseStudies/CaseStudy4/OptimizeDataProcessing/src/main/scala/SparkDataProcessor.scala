import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions.{avg, _}
import org.apache.spark.storage.StorageLevel

class SparkDataProcessor {

  def loadStaticData(spark: SparkSession, config: Configuration): (DataFrame, DataFrame) = {
    // Load store metadata (features) and sales data from  GCS
    val featuresDf = spark.read.option("header", "true").csv(config.featuresDataPath)
    val storesDf = spark.read.option("header", "true").csv(config.storesDataPath)

    val cleanedFeaturesDf = cleanFeaturesData(featuresDf).cache()
    val cleanedStoresData = cleanStoresData(storesDf)

    val broadcastedStores = broadcast(cleanedStoresData)

    (cleanedFeaturesDf, broadcastedStores)
  }

  def cleanFeaturesData(featuresDF: DataFrame): DataFrame = {
    featuresDF.filter(
      col("Store").isNotNull &&
        col("Date").isNotNull &&
        col("Temperature").isNotNull &&
        col("Fuel_Price").isNotNull
    ).drop("isHoliday")
  }

  // Clean Stores Data
  def cleanStoresData(storesDF: DataFrame): DataFrame = {
    storesDF.filter(
      col("Store").isNotNull &&
        col("Type").isNotNull &&
        col("Size").isNotNull
    )
  }

  def cleanSalesData(trainDF: DataFrame): DataFrame = {
    trainDF.filter(
      col("Store").isNotNull &&
        col("Dept").isNotNull &&
        col("Date").isNotNull &&
        col("Weekly_Sales").isNotNull &&
        col("IsHoliday").isNotNull
    ).filter(col("Weekly_Sales") >= 0)
  }

  def storeLevelMetrics(enrichedSalesDf: DataFrame, config: Configuration): Unit = {
    val storeSalesDf = enrichedSalesDf.groupBy("Store")
      .agg(
        sum("Weekly_Sales").alias("Total_Store_Sales"),
        avg("Weekly_Sales").alias("Average_Store_Sales")
      ).orderBy(desc("Total_Store_Sales")).cache()

    println("Store Sales Metrics:")
    storeSalesDf.show(10)

    println("Top 10 performing stores based on weekly sales: ")
    val topPerformingStores = storeSalesDf.limit(10).cache()
    topPerformingStores.show()

     storeSalesDf.write
       .mode("overwrite")
       .json(config.storeMetricsOutputPath)
    storeSalesDf.unpersist()

    topPerformingStores.write
      .mode("overwrite")
      .json(config.topPerfStoresOutputPath)
    topPerformingStores.unpersist()
  }

  def departmentLevelMetrics(enrichedSalesDf: DataFrame, config: Configuration): Unit = {
    val departmentSalesDf = enrichedSalesDf.groupBy("Store","Dept")
      .agg(
        sum("Weekly_Sales").alias("Total_Dept_Sales"),
        avg("Weekly_Sales").alias("Average_Dept_Sales"),
        collect_list("Weekly_Sales").as("Weekly_Sales_Trend")
      ).cache()

    println("Department Sales Metrics:")
    departmentSalesDf.show(10)

     departmentSalesDf.write
       .mode("overwrite")
       .json(config.deptMetricsOutputPath)

    departmentSalesDf.unpersist()

    val holidayVsNonHolidaySalesDf = enrichedSalesDf.groupBy("Store", "Dept")
      .agg(
        sum(when(col("IsHoliday") === "TRUE", col("Weekly_Sales")).otherwise(0)).alias("Total_Sales_Holiday"),
        sum(when(col("IsHoliday") === "FALSE", col("Weekly_Sales")).otherwise(0)).alias("Total_Sales_Non_Holiday"),
        avg(when(col("IsHoliday") === "TRUE", col("Weekly_Sales")).otherwise(0)).alias("Average_Sales_Holiday"),
        avg(when(col("IsHoliday") === "FALSE", col("Weekly_Sales")).otherwise(0)).alias("Average_Sales_Non_Holiday")
      )

    holidayVsNonHolidaySalesDf.cache()

    println("Holiday vs Non-Holiday Sales Metrics:")
    holidayVsNonHolidaySalesDf.show(10)

     holidayVsNonHolidaySalesDf.write
        .mode("overwrite")
        .json(config.deptIsHolidayOutputPath)

    holidayVsNonHolidaySalesDf.unpersist()
  }
}

object SparkDataProcessor {
  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
      .appName("WalmartSalesConsumer")
      .config("spark.hadoop.fs.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFileSystem")
      .config("spark.hadoop.fs.AbstractFileSystem.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFS")
      .config("spark.hadoop.google.cloud.auth.service.account.enable", "true")
      .config("spark.hadoop.google.cloud.auth.service.account.json.keyfile", "/Users/rupalgupta/gcp-final-key.json")
      .master("local[*]")
      .getOrCreate()

    val config = new Configuration()

    val sparkDataProcessor = new SparkDataProcessor()
    // Load static data (features and stores metadata)
    val (featuresDf, storesDf) = sparkDataProcessor.loadStaticData(spark, config)
    val trainDf = spark.read.option("header", "true").csv(config.trainDataPath)
    val cleanedSalesData = sparkDataProcessor.cleanSalesData(trainDf)

    val enrichedSalesDf = cleanedSalesData
      .join(featuresDf, Seq("Store", "Date"))
      .join(storesDf, "Store")
      .repartition(col("Store"), col("Date")).persist(StorageLevel.MEMORY_AND_DISK)

    // Perform aggregation
    sparkDataProcessor.storeLevelMetrics(enrichedSalesDf, config)
    sparkDataProcessor.departmentLevelMetrics(enrichedSalesDf, config)

    println("Enriched sales data:")
    enrichedSalesDf.show(10)
    enrichedSalesDf.write
      .mode("overwrite")
      .partitionBy("Store", "Date")
      .parquet(config.enrichedDataOutputPath)

    spark.stop()
  }
}
