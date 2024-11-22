package security

import org.apache.pekko.stream.Materializer
import play.api.http.HttpFilters
import play.api.mvc._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

// Filter for JWT Authentication
class JwtAuthFilter @Inject()(
                               implicit val mat: Materializer,
                               ec: ExecutionContext
                             ) extends Filter {
  override def apply(nextFilter: RequestHeader => Future[Result])(request: RequestHeader): Future[Result] = {
    val publicRoutes = Seq("/api/login")
    if (publicRoutes.exists(request.path.startsWith)) {
      // Allow public routes without authentication
      nextFilter(request)
    } else {
      val tokenOpt = request.headers.get("Authorization").map(_.replace("Bearer ", ""))
      println(tokenOpt)
      val tokenOptResult =JwtUtil.validateToken(tokenOpt.getOrElse(""))
      val userIdOpt = tokenOptResult match {
        case Some("rupalg") => Some("rupalg")
        case _ => None
      }
      userIdOpt match {
        case Some(userId) =>
          nextFilter(request)
        case None =>
          Future.successful(Results.Unauthorized("Invalid or missing token"))
      }
    }
  }
}
// Filters Registration
class Filters @Inject()(jwtAuthFilter: JwtAuthFilter) extends HttpFilters {
  override def filters: Seq[EssentialFilter] = Seq(jwtAuthFilter)
}