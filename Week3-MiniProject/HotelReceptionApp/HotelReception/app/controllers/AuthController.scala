package controllers
import play.api.mvc._
import play.api.libs.json._
import security.JwtUtil
import javax.inject.Inject

class AuthController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {
  def login: Action[JsValue] = Action(parse.json) { request =>
    val username = (request.body \ "rupalg").as[String]
    val password = (request.body \ "********").as[String]
    if (username == "admin" && password == "password") {
      val token = JwtUtil.generateToken(username)
      Ok(Json.obj("token" -> token))
    } else {
      Unauthorized("Invalid credentials")
    }
  }
}