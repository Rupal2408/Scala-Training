class Configuration {
  val kafkaBootstrapServers: String = sys.env.getOrElse("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092")
  val kafkaTopic: String = sys.env.getOrElse("KAFKA_TOPIC", "weekly_sales_topic")
  val featuresDataPath: String = sys.env.getOrElse("FEATURES_DATA_PATH", "gs://gcs_bucket_rupal/features.csv")
  val storesDataPath: String = sys.env.getOrElse("STORES_DATA_PATH", "gs://gcs_bucket_rupal/stores.csv")
  val trainDataPath: String = sys.env.getOrElse("TRAIN_DATA_PATH", "gs://gcs_bucket_rupal/train.csv")
  val storeMetricsOutputPath:String = sys.env.getOrElse("STORE_METRICS_OUTPUT_PATH","gs://gcs_bucket_rupal/store_metrics/")
  val topPerfStoresOutputPath:String = sys.env.getOrElse("TOP_PERF_STORES_OUTPUT_PATH","gs://gcs_bucket_rupal/top_performing_stores/")
  val deptMetricsOutputPath:String = sys.env.getOrElse("DEPT_METRICS_OUTPUT_PATH","gs://gcs_bucket_rupal/department_metrics/")
  val deptIsHolidayOutputPath:String = sys.env.getOrElse("DEPT_ISHOLIDAY_OUTPUT_PATH","gs://gcs_bucket_rupal/department_is_holiday_metrics/")
  val enrichedDataOutputPath:String = sys.env.getOrElse("ENRICHED_DATA_OUTPUT_PATH","gs://gcs_bucket_rupal/enriched_data/")
}
