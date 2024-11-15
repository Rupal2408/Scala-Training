import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import KafkaJsonFormats._
import spray.json.enrichAny

object Constants {
  val NETWORK_MESSAGE = "NetworkMessage"
  val APP_MESSAGE = "AppMessage"
  val CLOUD_MESSAGE = "CloudMessage"
}

class MessageHandler(networkMessageActor: ActorRef, appMessageActor: ActorRef, cloudMessageActor: ActorRef) extends Actor {
  def receive: Receive = {
    case Message(messageType, message, messageKey) =>
      messageType match {
        case Constants.NETWORK_MESSAGE =>
          println(s"The Message is of type ${Constants.NETWORK_MESSAGE}")
          networkMessageActor ! MessageProcess(message, messageKey)

        case Constants.APP_MESSAGE =>
          println(s"The Message is of type ${Constants.APP_MESSAGE}")
          appMessageActor ! MessageProcess(message, messageKey)

        case Constants.CLOUD_MESSAGE =>
          println(s"The Message is of type ${Constants.CLOUD_MESSAGE}")
          cloudMessageActor ! MessageProcess(message, messageKey)
      }
  }
}

class NetworkMessageActor(producer: KafkaProducer[String, String]) extends Actor {
  val topic = "network-message"
  def receive: Receive = {
    case message: MessageProcess =>
      println(s"Sending the kafka message to $topic topic")
      val jsonString = message.toJson.toString()
      val record = new ProducerRecord[String, String](topic, jsonString)
      producer.send(record)
      println(s"Message Sent to Kafka: $jsonString")
  }
}

class AppMessageActor(producer: KafkaProducer[String, String]) extends Actor {
  val topic = "app-message"
  def receive: Receive = {
    case message: MessageProcess =>
      println(s"Sending the kafka message to $topic topic")
      val jsonString = message.toJson.toString()
      val record = new ProducerRecord[String, String](topic, jsonString)
      producer.send(record)
      println(s"Message Sent to Kafka: $jsonString")
  }
}

class CloudMessageActor(producer: KafkaProducer[String, String]) extends Actor {
  val topic = "cloud-message"
  def receive: Receive = {
    case message: MessageProcess =>
      println(s"Sending the kafka message to $topic topic")
      val jsonString = message.toJson.toString()
      val record = new ProducerRecord[String, String](topic, jsonString)
      producer.send(record)
      println(s"Message Sent to Kafka: $jsonString")
  }
}

object KafkaActorProducer {

  implicit val actorsystem = ActorSystem("KafkaProducerSystem")

  val producer = KafkaProducerFactory.createProducer()

  val networkMessageActor = actorsystem.actorOf(Props(new NetworkMessageActor(producer)), "NetworkMessageActor")
  val appMessageActor = actorsystem.actorOf(Props(new AppMessageActor(producer)), "AppMessageActor")
  val cloudMessageActor = actorsystem.actorOf(Props(new CloudMessageActor(producer)), "CloudMessageActor")

  val MessageActorFactory = actorsystem.actorOf(Props(new MessageHandler(networkMessageActor, appMessageActor, cloudMessageActor)))

  def sendKafkaMessage(message: Message) = {
    MessageActorFactory ! message
  }
}
