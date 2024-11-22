package services

import models.entity.Task
import models.enums.TaskStatus
import models.enums.TaskStatus.TaskStatus
import models.request.AssignTasksRequest
import repositories.TaskRepository

import java.time.LocalDateTime
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TaskService @Inject() (
                              taskRepository: TaskRepository,
                              kafkaProducerFactory: KafkaProducerFactory
                            )(implicit executionContext: ExecutionContext) {

  def getTaskById(taskId: Long): Future[Task] = taskRepository.getTaskById(taskId)

  def updateStatus(taskId: Long, status: TaskStatus): Future[Task] = {
    taskRepository.updateStatus(taskId, status)
  }

  def getTasksForEventId(eventId: Long): Future[Seq[Task]] = taskRepository.getTasksForEventId(eventId)

  def assignTasks(req: AssignTasksRequest): Future[List[Task]] = {
    val tasksList = req.tasks.foldLeft(List.empty[Task])((acc, ele) => {
      val task = Task(
        eventId =  req.eventId,
        teamId = ele.teamId,
        taskDescription = ele.taskDescription,
        deadLine = ele.deadLine,
        specialInstructions = ele.specialInstructions,
        status = TaskStatus.ASSIGNED,
        createdAt = LocalDateTime.now().toString
      )
      acc :+ task
    })
    taskRepository.assignTasks(tasksList).map { lists =>
      kafkaProducerFactory.sendTasksAssignmentList(lists)
      lists
    }
  }

}