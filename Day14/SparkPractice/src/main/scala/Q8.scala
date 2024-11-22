import org.apache.spark.sql.SparkSession

object Q8 {
  case class CSVRecords(name: String, age: Int)

  implicit def convertToCSVRecords(str: String): CSVRecords = {
    val strList = str.split(",")
    CSVRecords(strList(0), strList(1).toInt)
  }

  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
      .appName("FilterAdults")
      .master("local[*]")
      .getOrCreate()

    val data = Seq(
      "Ross,24",
      "Monica,45",
      "Rachel,15",
      "Joey,14"
    )

    val rdd = spark.sparkContext.parallelize(data)

    val parsedRdd = rdd.map(row => convertToCSVRecords(row))

    val filteredRdd = parsedRdd.filter(_.age >= 18)

    filteredRdd.collect().foreach { record =>
      println(s"Name: ${record.name} with Age: ${record.age}")
    }

    spark.stop()
  }
}
