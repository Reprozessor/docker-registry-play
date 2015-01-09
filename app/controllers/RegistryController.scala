package controllers

import play.api.libs.iteratee.Enumerator
import play.api.libs.json._
import play.api.mvc._
import scala.collection.JavaConverters._

import scala.language.reflectiveCalls
import scala.util.{Failure, Success, Try}

import java.nio.file.{Paths, Files, StandardOpenOption}
import java.nio.charset.{StandardCharsets}

object RegistryController extends Controller {

  val dataPath = Paths.get("data")

  def _ping() = Action {
    Ok(Json.toJson(true)).withHeaders("X-Docker-Registry-Version" -> "0.6.3")
  }

  def images(repo: String) = Action {
    Ok(Json.toJson("a"))
  }

  def putImages(repo: String) = Action {
    // TODO
    Status(204)("")
  }

  def putRepo(repo: String) = Action {implicit request =>
    val host: String = request.headers.get("Host").getOrElse("")
    Ok(Json.toJson("PUTPUT")).withHeaders("X-Docker-Token" -> "mytok").withHeaders("X-Docker-Endpoints" -> host)
  }

  def getImageJson(image: String) = Action {
    val imagePath = dataPath.resolve(image + ".json")
    val layerPath = dataPath.resolve(image + ".layer")
    if (Files.exists(imagePath) && Files.exists(layerPath)) {
      val contents = Files.readAllLines(dataPath.resolve(image + ".json"), StandardCharsets.UTF_8).asScala.mkString
      Ok(Json.parse(contents))
    } else {
      NotFound("Image JSON not found")
    }
  }

  def putImageJson(image: String) = Action(BodyParsers.parse.json) { request =>
    Files.createDirectories(dataPath)
    val contents = Json.stringify(request.body)

    Files.write(dataPath.resolve(image + ".json"), contents.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE)
    Ok(Json.toJson("OK"))
  }

  def putImageLayer(image: String) = Action(BodyParsers.parse.temporaryFile) { request =>
    val layerPath = dataPath.resolve(image + ".layer")
    val bodyData = request.body.moveTo(layerPath.toFile)
    Ok(Json.toJson("OK"))
  }

  def putImageChecksum(image: String) = Action {
    // TODO: do something
    Ok(Json.toJson("OK"))
  }

  def putTag(repo: String, tag:String) = Action {
    // TODO: save tag
    Ok(Json.toJson("OK"))
  }
}

