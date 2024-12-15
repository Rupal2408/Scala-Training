package config

object Configuration {
  // Kafka settings
  val kafkaBootstrapServers: String = sys.env.getOrElse("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092")
  val kafkaTopic: String = sys.env.getOrElse("KAFKA_TOPIC", "movie_ratings")

  val gcsJsonKeyfile: String = sys.env.getOrElse("GCS_KEYFILE","/Users/rupalgupta/gcp-final-key.json")

  // Path to raw data in GCS
  val moviesDataPath: String = sys.env.getOrElse("MOVIES_DATA_PATH", "gs://gcs_bucket_rupal/movies.csv")
  val usersDataPath: String = sys.env.getOrElse("USERS_DATA_PATH", "gs://gcs_bucket_rupal/user_data.csv")

  // Path for storing aggregated metrics
  val aggregatedMovieMetricsPath: String = sys.env.getOrElse("AGGREGATED_MOVIE_METRICS_PATH","gs://gcs_bucket_rupal/case_study_2/aggregated_movie_metrics")
  val aggregatedGenreMetricsPath: String = sys.env.getOrElse("AGGREGATED_GENRE_METRICS_PATH","gs://gcs_bucket_rupal/case_study_2/aggregated_genre_metrics")
  val aggregatedUserDemographicsMetricsPath: String = sys.env.getOrElse("AGGREGATED_USER_DEMO_PATH","gs://gcs_bucket_rupal/case_study_2/aggregated_user_demographic_metrics")
  val enrichedDataPath: String = sys.env.getOrElse("ENRICHED_DATA_PATH","gs://gcs_bucket_rupal/case_study_2/enriched_ratings/")

}

