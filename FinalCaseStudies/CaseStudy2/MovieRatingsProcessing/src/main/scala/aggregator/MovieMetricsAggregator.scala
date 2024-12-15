package aggregator

import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, SparkSession}
import config.Configuration


 class MovieMetricsAggregator {
  def aggregate(enrichedDF: DataFrame): Unit = {

    val movieMetrics = enrichedDF
      .groupBy("movieId", "title", "genres")
      .agg(
        avg("rating").as("average_rating"),
        count("rating").as("total_ratings")
      )

    println("Aggregated Movie Metrics: ")
    movieMetrics.show(10)
    movieMetrics
      .write
      .mode("overwrite")
      .parquet(Configuration.aggregatedMovieMetricsPath)

    println("Successfully saved Aggregated Movie Metrics.")
  }
}
