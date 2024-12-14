import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer
import org.json4s.DefaultFormats
import org.json4s.native.Serialization.write
import java.util.Properties
import scala.util.Random

object KafkaProducer {
  implicit val formats: DefaultFormats.type = DefaultFormats

  def main(args: Array[String]): Unit = {
    // Kafka Producer configuration
    val kafkaBootstrapServers = "localhost:9092"
    val kafkaTopic = "weekly_sales_topic"

    val props = new Properties()
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers)
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)

    val producer = new KafkaProducer[String, String](props)

    // Simulate generating weekly sales data
    val stores = List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10) // Store IDs
    val departments = List(5, 10, 15, 20, 25, 30, 35, 40, 45, 50) // Department IDs

    while(true) {
      val store = stores(Random.nextInt(stores.length))
      val dept = departments(Random.nextInt(departments.length))
      val weeklySales = Random.nextDouble() * 10000  // Simulate sales amount
      val date = "2023-12-01" // Static date, can be dynamic for real scenario
      val isHoliday = scala.util.Random.nextBoolean().toString.toUpperCase

      val salesData = SalesData(store, dept, weeklySales, date, isHoliday)

      val message = write(salesData)

      val record = new ProducerRecord[String, String](kafkaTopic, store.toString, message)
      producer.send(record)
      println(s"Produced message: $message")

      Thread.sleep(1000)
    }

    producer.close()
  }
}
