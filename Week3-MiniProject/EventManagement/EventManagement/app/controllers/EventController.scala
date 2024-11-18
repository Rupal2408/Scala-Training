package controllers

import java.time.format.DateTimeParseException
import models.entity.Event
import models.enums.EventStatus
import play.api.mvc._
import play.api.libs.json._
import services.EventService
import java.time.LocalDate
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import models.response.ApiResponse

@Singleton
class EventController @Inject()(
                                 val cc: ControllerComponents,
                                 eventService: EventService
                               )(implicit ec: ExecutionContext) extends AbstractController(cc) {

  // Create an event
  def createEvent(): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[Event] match {
      case JsSuccess(event, _) =>
        eventService.create(event).map { created =>
          ApiResponse.successResult(201, Json.obj("message" -> "Event created", "id" -> created))
        }.recover {
          case ex: Exception =>
            ApiResponse.errorResult(s"Error creating event: $ex", 400)
        }
      case JsError(errors) =>
        Future.successful(ApiResponse.errorResult(
          "Invalid event request data",
          400
        ))
    }
  }

  // Get event details
  def getEventById(eventId: Long): Action[AnyContent] = Action.async {
    eventService.getEventById(eventId).map(created =>
      ApiResponse.successResult(200, Json.toJson(created)))
  }

  // Update event details
  def updateEvent(eventId: Long): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[Event] match {
      case JsSuccess(event, _) =>
        eventService.update(eventId, event).map(updated =>
          ApiResponse.successResult(200, Json.toJson(updated)))
      case JsError(errors) =>
        Future.successful(ApiResponse.errorResult(
          "Invalid updatedEvent request data",
          400
        ))
    }
  }

  // Get the list of events
  def listEvents(eventType: Option[String], status: Option[String], eventDate: Option[String], slotNumber: Option[Int])
  : Action[AnyContent] = Action.async {
    val parsedDate: Option[LocalDate] = eventDate.flatMap { date =>
      try {
        Some(LocalDate.parse(date))
      } catch {
        case _: DateTimeParseException => None
      }
    }
    val statusEnum = EventStatus.withNameOption(status)
    eventService.list(eventType, statusEnum, parsedDate, slotNumber).map(response =>
      ApiResponse.successResult(200, Json.toJson(response))
    )
  }

  // Get tasks for an eventId
  def getTasksForEventId(eventId: Long): Action[AnyContent] = Action.async {
    eventService.getTasksForEventId(eventId).map {response =>
      ApiResponse.successResult(200, Json.toJson(response))
    }
  }
}