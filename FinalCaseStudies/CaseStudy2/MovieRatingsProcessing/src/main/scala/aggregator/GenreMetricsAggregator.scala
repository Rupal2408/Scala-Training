package aggregator

import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, SparkSession}
import config.Configuration


class GenreMetricsAggregator {
  def aggregate(enrichedDF: DataFrame): Unit = {

    val genreMetrics = enrichedDF
      .withColumn("genre", explode(split(col("genres"), "|")))
      .groupBy("genre")
      .agg(
        avg("rating").as("average_rating"),
        count("rating").as("total_ratings")
      )

    println("Aggregated Genre Metrics: ")
    genreMetrics.show(10)
    genreMetrics
      .write
      .mode("overwrite")
      .parquet(Configuration.aggregatedGenreMetricsPath)

    println("Successfully saved Aggregated Genre Metrics.")
  }
}
