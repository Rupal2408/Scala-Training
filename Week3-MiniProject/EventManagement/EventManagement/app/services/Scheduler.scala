package services

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import repositories.{EventRepository, TaskRepository}
import java.time.{Duration, LocalDate, LocalDateTime, LocalTime}
import java.util.concurrent.{Executors, ScheduledExecutorService, TimeUnit}

class Scheduler @Inject()(eventRepository: EventRepository,
                          kafkaProducerFactory: KafkaProducerFactory,
                          taskRepository: TaskRepository)(implicit ec: ExecutionContext) {

  // Initial time after the application startup when this runs
  private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)
  startDailyOverdueCheck()

  private def startDailyOverdueCheck(): Unit = {
    // Schedule the task to run daily at the specified time
    scheduler.scheduleAtFixedRate(
      new Runnable {
        override def run(): Unit = {
          checkEventDayAlert()
        }
      },
      0L,
      TimeUnit.DAYS.toSeconds(1), // Repeat every 24 hours
      TimeUnit.SECONDS
    )
  }


  // Method to check for Event Day Alert
  private def checkEventDayAlert(): Unit = {
    val currentDate = LocalDate.now()

    eventRepository.getEventsByDate(currentDate: LocalDate).flatMap { events =>
      Future.sequence(
        events.map { event =>
          taskRepository.getTasksForEventId(event.id.get).map { tasks =>
            kafkaProducerFactory.sendEventAlerts(event, tasks, isEventDay = true)
          }
        }
      )
    }.recover {
      case ex: Exception =>
        println(s"Failed to check overdue allocations: ${ex.getMessage}")
    }
  }

}