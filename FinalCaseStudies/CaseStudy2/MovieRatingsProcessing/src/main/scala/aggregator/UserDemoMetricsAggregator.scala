package aggregator

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions.{avg, col, count, explode, split}

class UserDemoMetricsAggregator {
  def aggregate(enrichedDF: DataFrame): Unit = {

    val userDemographicsMetrics = enrichedDF
      .groupBy("age","gender","location")
      .agg(
        avg("rating").as("average_rating"),
        count("rating").as("total_ratings")
      )

    println("Aggregated Genre Metrics: ")
    userDemographicsMetrics.show(10)
    userDemographicsMetrics.limit(100)
      .write
      .mode("overwrite")
      .parquet("gs://gcs_bucket_rupal/case_study_2/aggregated_user_demographic_metrics/")

    println("Successfully saved Aggregated User Demographic Metrics.")
  }
}
