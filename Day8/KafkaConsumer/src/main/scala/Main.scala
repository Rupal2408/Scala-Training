import Constants._
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.scaladsl.Sink
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import JsonFormats._
import KafkaConsumerActors._
import spray.json._

object Main {

  def main(args: Array[String]): Unit = {
    implicit val actorSystem: ActorSystem[_] = ActorSystem(Behaviors.empty, "kafkaConsumerSystem")
      val consumerSettings = ConsumerSettings(actorSystem, new StringDeserializer, new StringDeserializer)
      .withBootstrapServers(sys.env.get("BROKER_HOST").getOrElse("localhost")+":9092")
      .withGroupId("group1")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest")

    val topics = Set(NETWORK_MESSAGE, APP_MESSAGE, CLOUD_MESSAGE)

    Consumer.plainSource(consumerSettings, Subscriptions.topics(topics))
      .map(record => {
        val msg = record.value().parseJson.convertTo[Message]
        record.topic() match {
          case NETWORK_MESSAGE => networkListener ! msg
          case APP_MESSAGE => appListener ! msg
          case CLOUD_MESSAGE => cloudListener ! msg
        }
      })
      .runWith(Sink.ignore)
  }
}