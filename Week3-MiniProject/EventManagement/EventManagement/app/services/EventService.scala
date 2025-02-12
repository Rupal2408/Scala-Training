package services

import models.entity.{Event, Task}
import models.enums.EventStatus
import models.enums.EventStatus.EventStatus
import repositories.EventRepository
import java.time.LocalDate
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EventService @Inject() (
                               eventRepository: EventRepository,
                               taskService: TaskService
                             )(implicit ex:ExecutionContext) {
  def create(event: Event): Future[Long] = {
    val updatedEvent = event.copy(eventStatus = Some(EventStatus.SCHEDULED))
    eventRepository.create(updatedEvent)
  }

  def getEventById(eventId: Long): Future[Event] = eventRepository.getEventById(eventId)

  def update(eventId: Long, event: Event): Future[Event] = eventRepository.update(eventId, event)

  def list(eventType: Option[String], status: Option[EventStatus], eventDate: Option[LocalDate])
  : Future[Seq[Event]] = eventRepository.listEvents(eventType, status, eventDate)

  def getTasksForEventId(eventId: Long): Future[Seq[Task]] = taskService.getTasksForEventId(eventId)
}