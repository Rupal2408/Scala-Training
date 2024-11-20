package controllers

import models.entity.Task
import models.enums.TaskStatus
import models.request.AssignTasksRequest
import models.response.ApiResponse
import play.api.mvc._
import play.api.libs.json._
import services.TaskService
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TaskController @Inject()(
                                val cc: ControllerComponents,
                                taskService: TaskService
                              )(implicit ec: ExecutionContext) extends AbstractController(cc) {

  // Get task details
  def getTaskById(taskId: Long): Action[AnyContent] = Action.async {
    taskService.getTaskById(taskId).map(created =>
      ApiResponse.successResult(200, Json.toJson(created)))
  }

  // Update task details
  def updateTaskStatus(taskId: Long, status: String): Action[AnyContent] = Action.async {
    val taskStatus = TaskStatus.withNameOption(Some(status))
    taskService.updateStatus(taskId, taskStatus.get).map(updated =>
      ApiResponse.successResult(200, Json.toJson(updated)))
  }

  // Assign tasks
  def assignTasks(): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[AssignTasksRequest] match {
      case JsSuccess(req, _) =>
        taskService.assignTasks(req).map(created =>
          ApiResponse.successResult(200, Json.toJson(created))
        )
      case JsError(errors) =>
        Future.successful(ApiResponse.errorResult(
          "Invalid task request data",
          400
        ))
    }
  }

}