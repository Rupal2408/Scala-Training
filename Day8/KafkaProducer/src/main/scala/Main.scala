import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import JsonFormats._
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

object Main {
  implicit val actorSystem = ActorSystem(Behaviors.empty, "MyActorSystem")

  def main(args: Array[String]): Unit = {
    val route =
      post {
        path("process-Message") {
          entity(as[Message]) { message =>
            KafkaActorProducer.sendKafkaMessage(message)
            complete(StatusCodes.OK, s"Message sent to Kafka: $message")
          }
        }
    }

    Http().newServerAt("0.0.0.0", 8080).bind(route)
    println("Server is now online at http://0.0.0.0:8080/")
  }
}
