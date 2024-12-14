package aggregator

import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, SparkSession}

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
      .parquet("gs://gcs_bucket_rupal/case_study_2/aggregated_movie_metrics/")

    println("Successfully saved Aggregated Movie Metrics.")
  }
}
