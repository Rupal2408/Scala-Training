package aggregator

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions.{avg, col, count, explode, split}
import config.Configuration

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
    userDemographicsMetrics
      .write
      .mode("overwrite")
      .parquet(Configuration.aggregatedUserDemographicsMetricsPath)

    println("Successfully saved Aggregated User Demographic Metrics.")
  }
}
