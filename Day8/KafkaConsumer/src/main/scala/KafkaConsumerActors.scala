import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import spray.json.enrichAny
import JsonFormats._

import java.util.Properties

object Constants {
  val NETWORK_MESSAGE = "network-message"
  val APP_MESSAGE = "app-message"
  val CLOUD_MESSAGE = "cloud-message"
}

class MessageGathererActor(producer: KafkaProducer[String, String]) extends Actor {
  def receive: Receive = {
    case message: Message =>
      println("MessageGatherer received the message")
      val jsonString = message.toJson.toString()
      val record = new ProducerRecord[String, String]("consolidated-messages", message.messageKey, message.toJson.toString())
      producer.send(record)
      println(s"Produced $jsonString to consolidated-message topic")
  }
}

class NetworkKafkaListener(messageGatherer: ActorRef) extends Actor {
  def receive: Receive = {
    case message: Message =>
      println("Network Listener consumed the message")
      messageGatherer ! message
  }
}

class AppKafkaListener(messageGatherer: ActorRef) extends Actor {
  def receive: Receive = {
    case message: Message =>
      println("App Listener consumed the message")
      messageGatherer ! message
  }
}

class CloudKafkaListener(messageGatherer: ActorRef) extends Actor {
  def receive: Receive = {
    case message: Message =>
      println("Cloud Listener consumed the message")
      messageGatherer ! message
  }
}
object KafkaConsumerActors {
    val props = new Properties()
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, sys.env.get("BROKER_HOST").getOrElse("localhost")+":9092")
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    val producer = new KafkaProducer[String, String](props)

    val actorSystem = ActorSystem("KafkaConsumerSystem")
    val messageGathererActor = actorSystem.actorOf(Props(new MessageGathererActor(producer)), "MessageGatherer")
    val networkListener = actorSystem.actorOf(Props(new NetworkKafkaListener(messageGathererActor)), "NetworkKafkaListener")
    val appListener = actorSystem.actorOf(Props(new AppKafkaListener(messageGathererActor)), "AppKafkaListener")
    val cloudListener = actorSystem.actorOf(Props(new CloudKafkaListener(messageGathererActor)), "CloudKafkaListener")

}
