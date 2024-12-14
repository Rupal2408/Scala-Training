package producer

import akka.actor.{Actor, ActorSystem, Props}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

import scala.util.Random
import java.text.SimpleDateFormat
import java.util.Date
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

class RatingProducerActor(kafkaProducer: KafkaProducer[String, String], topic: String) extends Actor {
  val random = new Random()

  def receive = {
    case "generateMovies" =>
      try {
        while (true) {
          val userId = random.nextInt(1000) + 1
          val movieId = random.nextInt(100) + 1
          val rating = (random.nextInt(9) + 1) * 0.5
          val timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())

          val ratingJson =
            s"""{
        "userId": $userId,
        "movieId": $movieId,
        "rating": $rating,
        "timestamp": "$timestamp"
      }"""

          kafkaProducer.send(new ProducerRecord[String, String](topic, s"user_$userId", ratingJson))
          println(s"Produced movie rating: $ratingJson")
          Thread.sleep(50)
        }
      } finally {
        kafkaProducer.close()
      }
  }
}
