import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

case class MessageProcess(message: String, messageKey: String)

object KafkaJsonFormats {
  implicit val messageFormat: RootJsonFormat[MessageProcess] = jsonFormat2(MessageProcess)
}