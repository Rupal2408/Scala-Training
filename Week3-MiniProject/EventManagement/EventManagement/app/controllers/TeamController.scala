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