package controllers

import play.api.libs.json._
import play.api.mvc._
import scala.collection.JavaConverters._

import scala.language.reflectiveCalls
import scala.util.{Failure, Success, Try}

import java.nio.file.{Paths, Files, StandardOpenOption}
import java.nio.charset.{StandardCharsets}

object DockerHeaders {
  val REGISTRY_VERSION = "X-Docker-Registry-Version"
  val TOKEN            = "X-Docker-Token"
  val ENDPOINTS        = "X-Docker-Endpoints"
}
import DockerHeaders._

object RegistryController extends Controller {

  val dataPath = Paths.get("data")

  private def asJson(r: Result): Result = r.as("application/json")

  def _ping() = Action {
    asJson(Ok(Json.toJson(true)).withHeaders(REGISTRY_VERSION -> "0.6.3"))
  }

  def images(repo: String) = Action {
    NotImplemented
  }

  def putImages(repo: String) = Action {
    NotImplemented
  }

  def putRepo(repo: String) = Action {request =>
    val host: String = request.headers.get("Host").getOrElse("")
    asJson(
      Ok(Json.toJson("PUTPUT"))
      .withHeaders(TOKEN -> "mytok")
      .withHeaders(ENDPOINTS -> host)
    )
  }

  def getImageJson(image: String) = Action {
    val imagePath = dataPath.resolve(s"${image}.json")
    val layerPath = dataPath.resolve(s"${image}.layer")
    if (Files.exists(imagePath) && Files.exists(layerPath)) {
      val contents = Files.readAllLines(imagePath, StandardCharsets.UTF_8).asScala.mkString
      asJson(Ok(Json.parse(contents)))
    } else {
      asJson(NotFound(Json.toJson(s"Image JSON (${image}.json) not found")))
    }
  }

  def putImageJson(image: String) = Action(BodyParsers.parse.json) { request =>
    Files.createDirectories(dataPath)
    val contents: String = Json.stringify(request.body)

    Files.write(dataPath.resolve(s"${image}.json"), contents.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE)
    asJson(Ok(Json.toJson("OK")))
  }

  def putImageLayer(image: String) = Action(BodyParsers.parse.temporaryFile) { request =>
    val layerPath = dataPath.resolve(s"${image}.layer")
    val bodyData = request.body.moveTo(layerPath.toFile)
    asJson(Ok(Json.toJson("OK")))
  }

  def putImageChecksum(image: String) = Action {
    NotImplemented
    // TODO: do something
  }

  def putTag(repo: String, tag:String) = Action {
    NotImplemented
    // TODO: save tag
  }
}

