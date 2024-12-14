package producer

import akka.actor.ActorSystem
import akka.actor.Props
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig}

import java.util.Properties

object MovieRatingsProducerApp {
  def main(args: Array[String]): Unit = {
    val props = new Properties()
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")

    val producer = new KafkaProducer[String, String](props)

    val system = ActorSystem("MovieRatingsSystem")
    val movieRatingsActor = system.actorOf(Props(new RatingProducerActor(producer, "movie-ratings")))

    movieRatingsActor ! "generateMovies"
  }
}
