package controllers

import models.entity.Team
import models.enums.TeamType
import models.response.ApiResponse
import play.api.mvc._
import play.api.libs.json._
import services.TeamService
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TeamController @Inject()(
                                val cc: ControllerComponents,
                                teamService: TeamService
                              )(implicit ec: ExecutionContext) extends AbstractController(cc) {

  // Register a team
  def registerTeam(): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[Team] match {
      case JsSuccess(team, _) =>
        teamService.create(team).map(created =>
          ApiResponse.successResult(201, Json.obj("message"->"Team created", "id"-> created)))
      case JsError(errors) =>
        Future.successful(ApiResponse.errorResult(
          "Invalid team request data",
          400
        ))
    }
  }

  // Get Team Details
  def getTeamDetails(teamId: Long): Action[AnyContent] = Action.async {
    teamService.getTeamDetailsById(teamId).map(created =>
      ApiResponse.successResult(200, Json.toJson(created)))
  }

  // List Teams
  def listTeams(teamType: Option[String]): Action[AnyContent] = Action.async {
    val teamTypeEnum = TeamType.withNameOption(teamType)
    teamService.listTeams(teamTypeEnum).map(created =>
      ApiResponse.successResult(200, Json.toJson(created)))
  }
}