import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.col

import scala.util.Random

object Q4 {
  def generateDataset(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("Generate Data GCP Cloud")
      .config("spark.hadoop.fs.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFileSystem")
      .config("spark.hadoop.fs.AbstractFileSystem.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFS")
      .config("spark.hadoop.google.cloud.auth.service.account.enable", "true")
      .config("spark.hadoop.google.cloud.auth.service.account.json.keyfile", "/Users/rupalgupta/gcp-final-key.json")
      .master("local[*]")
      .getOrCreate()

    val gcsPath = "gs://scala_assgn_bucket/Day18and19Tasks/question4_input.csv"

    import spark.implicits._

    val random = new Random()
    val data = (1 to 10000).map { id =>
      val status = if(random.nextBoolean()) "completed" else "pending"
      val amount = random.nextDouble() * 1000 
      (id, status, amount)
    }

    val df = data.toDF("id", "status", "amount")

    df.write
      .mode("overwrite") 
      .parquet(gcsPath)

    println(s"Parquet file successfully written to $gcsPath")
    spark.stop()
  }

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("Filter Data GCP Cloud")
      .config("spark.hadoop.fs.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFileSystem")
      .config("spark.hadoop.fs.AbstractFileSystem.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFS")
      .config("spark.hadoop.google.cloud.auth.service.account.enable", "true")
      .config("spark.hadoop.google.cloud.auth.service.account.json.keyfile", "/Users/rupalgupta/gcp-final-key.json")
      .master("local[*]")
      .getOrCreate()

    val gcsInputPath = "gs://scala_assgn_bucket/Day18and19Tasks/question4_input.csv"
    val gcsOutputPath = "gs://scala_assgn_bucket/Day18and19Tasks/question4_output.csv"

    val transactionInputDF = spark.read.parquet(gcsInputPath)

    val processedData = transactionInputDF.filter(col("status") === "completed")

    processedData.write.mode("overwrite").parquet(gcsOutputPath)

    println(s"Processed data successfully written to $gcsOutputPath")
    spark.stop()


  }
}