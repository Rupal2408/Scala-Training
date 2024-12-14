package config

object Config {
  // Kafka settings
  val kafkaBootstrapServers: String = sys.env.getOrElse("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092")
  val kafkaTopic: String = sys.env.getOrElse("KAFKA_TOPIC", "movie_ratings")

  // GCS settings
  val gcsBucket = "gs://gcs_bucket_rupal"

  // Path to store checkpoint data for Spark Streaming
  val checkpointLocation = "gs://gcs_bucket_rupal/case_study_2/checkpoints"

  // Path for storing aggregated metrics
  val aggregatedMovieMetricsPath = "gs://gcs_bucket_rupal/case_study_2/aggregated_movie_metrics"
  val aggregatedGenreMetricsPath = "gs://gcs_bucket_rupal/case_study_2/aggregated_genre_metrics"
}

