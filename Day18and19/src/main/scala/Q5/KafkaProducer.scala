package Q5

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import scala.concurrent.ExecutionContext.Implicits.global
import java.util.Properties
import java.util.concurrent.atomic.AtomicBoolean
import scala.concurrent.Future
import scala.util.Random

object KafkaProducer {
  def main(args: Array[String]): Unit = {

    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

    val producer = new KafkaProducer[String, String](props)

    val random = new Random()

    def sendRecord(): Unit = {
      val orderId = random.nextInt(100) + 1          
      val userId = random.nextInt(1000) + 1         
      val amount = random.nextDouble() * 1000        

      val message = s"""{"orderId": $orderId, "userId": $userId, "amount": $amount}"""

      val record = new ProducerRecord[String, String]("orders", null, message)
      producer.send(record)

      println(s"Sent Message: $message")
    }

    println("Kafka producer started. Sending messages every 1 second...")
    val isRunning = new AtomicBoolean(true) 
    Future {
      while (isRunning.get()) {
        sendRecord()
        Thread.sleep(1000) 
      }
    }

    sys.addShutdownHook {
      println("Shutting down Kafka producer.")
      isRunning.set(false) 
      producer.close()
    }

    while (isRunning.get()) {
      Thread.sleep(100) 
    }
    println("Stopped Kafka producer")
  }
}