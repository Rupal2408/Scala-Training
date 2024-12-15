package api

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.model.headers.{`Access-Control-Allow-Headers`, `Access-Control-Allow-Methods`, `Access-Control-Allow-Origin`}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.desc
import spray.json.{DefaultJsonProtocol, enrichAny}
import config.Configuration

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

object MovieMetricsApi {
  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem("MetricsApiServer")
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    val spark = SparkSession.builder()
      .appName("MetricsApiServer")
      .config("spark.hadoop.fs.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFileSystem")
      .config("spark.hadoop.fs.AbstractFileSystem.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFS")
      .config("spark.hadoop.google.cloud.auth.service.account.enable", "true")
      .config("spark.hadoop.google.cloud.auth.service.account.json.keyfile", Configuration.gcsJsonKeyfile)
      .master("local[*]")
      .getOrCreate()

    // Load aggregated data
    val movieMetricsPath = Configuration.aggregatedMovieMetricsPath
    val genreMetricsPath = Configuration.aggregatedGenreMetricsPath
    val userDemoMetricsPath = Configuration.aggregatedUserDemographicsMetricsPath

    // API routes
    val route =
      pathPrefix("api") {
        concat(
          path("movie-metrics") {
            get {
              val movieMetricsDF = spark.read.parquet(movieMetricsPath)
              val metrics = movieMetricsDF.sort(desc("average_rating")).limit(100).toJSON.collect()
              respondWithHeaders(
                `Access-Control-Allow-Origin`.*,
                `Access-Control-Allow-Methods`(akka.http.scaladsl.model.HttpMethods.GET, akka.http.scaladsl.model.HttpMethods.POST),
                `Access-Control-Allow-Headers`("Content-Type", "Authorization")
              ) {
                complete(HttpResponse(
                  status = StatusCodes.OK,
                  entity = HttpEntity(ContentTypes.`application/json`, metrics.mkString("[", ",", "]"))
                ))
              }
            }
          },
          path("genre-metrics") {
            get {
              val genreMetricsDF = spark.read.parquet(genreMetricsPath)
              val metrics = genreMetricsDF.sort(desc("average_rating")).limit(100).toJSON.collect()
              respondWithHeaders(
                `Access-Control-Allow-Origin`.*,
                `Access-Control-Allow-Methods`(akka.http.scaladsl.model.HttpMethods.GET, akka.http.scaladsl.model.HttpMethods.POST),
                `Access-Control-Allow-Headers`("Content-Type", "Authorization")
              ) {
                complete(HttpResponse(
                  status = StatusCodes.OK,
                  entity = HttpEntity(ContentTypes.`application/json`, metrics.mkString("[", ",", "]"))
                ))
              }
            }
          },
          path("demographics-metrics") {
            get {
              val userDemoMetricsDF = spark.read.parquet(userDemoMetricsPath)
              val metrics = userDemoMetricsDF.sort(desc("average_rating")).limit(100).toJSON.collect()
              respondWithHeaders(
                `Access-Control-Allow-Origin`.*,
                `Access-Control-Allow-Methods`(akka.http.scaladsl.model.HttpMethods.GET, akka.http.scaladsl.model.HttpMethods.POST),
                `Access-Control-Allow-Headers`("Content-Type", "Authorization")
              ) {
                complete(HttpResponse(
                  status = StatusCodes.OK,
                  entity = HttpEntity(ContentTypes.`application/json`, metrics.mkString("[", ",", "]"))
                ))
              }
            }
          }
        )
      }

    // Start server
    val bindingFuture = Http().newServerAt("localhost", 8080).bindFlow(route)
    println("Server is online at http://localhost:8080/")

    bindingFuture.onComplete {
      case Success(binding) =>
        println(s"Server is running at http://${binding.localAddress.getHostString}:${binding.localAddress.getPort}/")
      case Failure(exception) =>
        println(s"Failed to start server: ${exception.getMessage}")
        system.terminate()
    }
  }
}
