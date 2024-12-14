package retention

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{col, from_unixtime, lit}

import java.time.Instant
import java.time.temporal.ChronoUnit

object RetentionPolicy {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("Data Retention Policy")
      .master("local[*]")
      .config("spark.hadoop.google.cloud.auth.service.account.enable", "true")
      .getOrCreate()

    spark.sparkContext.setLogLevel("ERROR")

    val enrichedDataPath = "gs://gcs_bucket_rupal/case_study_2/enriched_ratings/"
    val parquetDataPath = s"$enrichedDataPath"
    val retentionDays = 14

    // Retain enriched raw data for the last 14 days
    cleanupFiles(parquetDataPath, retentionDays)

    println("Retention policy applied successfully.")
  }

  def cleanupFiles(path: String, retentionDays: Int): Unit = {
    val now = Instant.now()
    val cutoffDate = now.minus(retentionDays, ChronoUnit.DAYS)
    val cutoffTimestamp = cutoffDate.toEpochMilli / 1000

    println(s"Cleaning up files older than $retentionDays days.")

    try {
      val files = SparkSession.active.read.format("binaryFile").load(path)

      val oldFiles = files
        .select("path", "modificationTime")
        .filter(col("modificationTime") < from_unixtime(lit(cutoffTimestamp)))

      oldFiles.select("path").collect().foreach { row =>
        val filePath = row.getString(0)
        println(s"Deleting file: $filePath")
        val fs = org.apache.hadoop.fs.FileSystem.get(SparkSession.active.sparkContext.hadoopConfiguration)
        val hadoopPath = new org.apache.hadoop.fs.Path(filePath)
        fs.delete(hadoopPath, false)
      }

      println("File cleanup completed.")
    } catch {
      case e: Exception =>
        println(s"Error during cleanup: ${e.getMessage}")
    }
  }
}
