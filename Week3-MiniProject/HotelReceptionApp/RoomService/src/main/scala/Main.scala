import actors.MailSenderActorSystem.mailSenderActor
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.scaladsl.Sink
import models.{Email, GuestInfo}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import models.JsonFormats._
import spray.json._

object Main {
  private def composeMail(guestInfo: GuestInfo): Email = {
    val body: String = s"Thank you for choosing to stay with us at Hotel Grand, RupalWork.\n\nWe look forward to making your" +
      s" stay a comfortable and memorable one! For your convenience, please find below some important contact details:" +
      s"\n\nEmergency Numbers: \n\nFront Desk: +91 XXXXXXXXXX\n\nRestaraunt Food Order: +91 XXXXXXXXXX\n\nFor any Emergency:" +
      s" +91 XXXXXXXXXX\n\nBest Regards,\nHotel Grand"
    Email(guestInfo.email, "Welcome Onboard!!!", body)
  }


  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem[_] = ActorSystem(Behaviors.empty, "HotelRoomServiceNotification")

    val consumerSettings = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
      .withBootstrapServers(sys.env.get("BROKER_HOST").getOrElse("localhost")+":9092")
      .withGroupId("roomServiceGroup")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest")

    Consumer.plainSource(consumerSettings, Subscriptions.topics("reservation-topic"))
      .map(record => {
        val guest = record.value().parseJson.convertTo[GuestInfo]
        composeMail(guest)
      }) // Convert JSON string to Person
      .runWith(Sink.actorRef(mailSenderActor,
        onCompleteMessage = s"Welcome mail sent to guest",
        onFailureMessage = throwable => s"Error occured: ${throwable.getMessage}"
      ))
  }
}