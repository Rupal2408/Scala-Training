import org.apache.spark.sql.SparkSession
import scala.util.Random

object Q1 {
  def generateDataset(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("Broadcast Join Example")
      .config("spark.hadoop.fs.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFileSystem")
      .config("spark.hadoop.fs.AbstractFileSystem.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFS")
      .config("spark.hadoop.google.cloud.auth.service.account.enable", "true")
      .config("spark.hadoop.google.cloud.auth.service.account.json.keyfile", "/Users/rupalgupta/gcp-final-key.json")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._

    def generateUserDetails(numUsers: Int): Seq[(Int, String)] = {
      val random = new Random()
      (1 to numUsers).map { id =>
        (id, s"User_${random.alphanumeric.take(5).mkString}")
      }
    }

    def generateTransactionLogs(numTransactions: Int, numUsers: Int): Seq[(Int, String, Double)] = {
      val random = new Random()
      (1 to numTransactions).map { _ =>
        val userId = random.nextInt(numUsers) + 1 
        val transactionId = s"txn_${random.alphanumeric.take(6).mkString}" 
        val amount = random.nextDouble() * 1000 
        (userId, transactionId, amount)
      }
    }

    val numUsers = 100       
    val numTransactions = 10000 

    val userDetailsDF = generateUserDetails(numUsers).toDF("userId", "userName")
    val transactionLogsDF = generateTransactionLogs(numTransactions, numUsers).toDF("userId", "transactionId", "amount")

    val userDetailsPath = "gs://scala_assgn_bucket/Day18and19Tasks/user_details.csv"
    val transactionLogsPath = "gs://scala_assgn_bucket/Day18and19Tasks/transaction_logs.csv"

    userDetailsDF.write
      .option("header", "true")
      .csv(userDetailsPath)

    transactionLogsDF.write
      .option("header", "true")
      .csv(transactionLogsPath)

    println(s"Data successfully written to GCS at $userDetailsPath and $transactionLogsPath")

    spark.stop()
  }

  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
      .appName("Broadcast Join Example")
      .config("spark.hadoop.fs.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFileSystem")
      .config("spark.hadoop.fs.AbstractFileSystem.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFS")
      .config("spark.hadoop.google.cloud.auth.service.account.enable", "true")
      .config("spark.hadoop.google.cloud.auth.service.account.json.keyfile", "/Users/rupalgupta/gcp-final-key.json")
      .master("local[*]")
      .getOrCreate()

    val userDetailsPath = "gs://scala_assgn_bucket/Day18and19Tasks/user_details.csv"
    val transactionLogsPath = "gs://scala_assgn_bucket/Day18and19Tasks/transaction_logs.csv"

    val userDetails = spark.read
      .option("header", "true") 
      .option("inferSchema", "true")
      .csv(userDetailsPath)
      .toDF("user_id", "name")

    val transactionLogs = spark.read
      .option("header", "true")
      .option("inferSchema", "true")
      .csv(transactionLogsPath)
      .toDF("user_id", "transaction_type", "amount")

    val joinedDataDF = transactionLogs
      .join(org.apache.spark.sql.functions.broadcast(userDetails), Seq("user_Id"))

    println("Data:")
    joinedDataDF.show(10)

    spark.stop()
  }
}