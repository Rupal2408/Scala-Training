import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

case class Message(message: String, messageKey: String)

object JsonFormats {
  implicit val messageFormat: RootJsonFormat[Message] = jsonFormat2(Message)
}
