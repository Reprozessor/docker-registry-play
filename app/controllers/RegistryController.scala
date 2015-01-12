package controllers

import play.api.libs.iteratee.Enumerator
import play.api.libs.json._
import play.api.mvc._
import scala.collection.JavaConverters._

import scala.language.reflectiveCalls
import scala.util.{Failure, Success, Try}

import java.nio.file.{Path, Paths, Files, StandardOpenOption}
import java.nio.charset.{StandardCharsets}

object DockerHeaders {
  val REGISTRY_VERSION    = "X-Docker-Registry-Version"
  val REGISTRY_STANDALONE = "X-Docker-Registry-Standalone"
  val TOKEN               = "X-Docker-Token"
  val ENDPOINTS           = "X-Docker-Endpoints"
}
import DockerHeaders._

object RegistryController extends Controller {

  val dataPath = Paths.get("data")

  private def asJson(f: => Result): Result = (f: Result).as("application/json")


  def root() = Action {
    asJson {
      Ok(Json.toJson("docker-registry-play server"))
    }
  }

  def _ping() = Action {
    asJson {
      Ok(Json.toJson(true))
        .withHeaders(REGISTRY_VERSION -> "0.6.3")
        .withHeaders(REGISTRY_STANDALONE -> "True")
    }
  }

  def images(repo: String) = Action {
    asJson { NotImplemented }
  }

  def putImages(repo: String) = Action {
    asJson { NotImplemented }
  }

  def putRepo(repo: String) = Action {request =>
    val host: String = request.headers.get("Host").getOrElse("")
    asJson {
      Ok(Json.toJson("PUTPUT"))
        .withHeaders(TOKEN -> "mytok")
        .withHeaders(ENDPOINTS -> host)
    }
  }

  def getImageJson(image: String) = Action {
    asJson {
      val imagePath: Path = dataPath.resolve(s"${image}.json")
      val layerPath = dataPath.resolve(s"${image}.layer")
      if (Files.exists(imagePath) && Files.exists(layerPath)) {
        val contents = Files.readAllLines(imagePath, StandardCharsets.UTF_8).asScala.mkString
        Ok(Json.parse(contents))
      } else {
        NotFound(Json.toJson(s"Image JSON (${image}.json) not found"))
      }
    }
  }

  def putImageJson(image: String) = Action(BodyParsers.parse.json) { request =>
    asJson {
      Files.createDirectories(dataPath)
      val contents: String = Json.stringify(request.body)

      Files.write(dataPath.resolve(s"${image}.json"), contents.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE)
      Ok(Json.toJson("OK"))
    }
  }

  def putImageLayer(image: String) = Action(BodyParsers.parse.temporaryFile) { request =>
    asJson {
      val layerPath = dataPath.resolve(s"${image}.layer")
      val bodyData = request.body.moveTo(layerPath.toFile)
      Ok(Json.toJson("OK"))
    }
  }

  def putImageChecksum(image: String) = Action {
    asJson {
      NotImplemented
      // TODO: do something
    }
  }

  def putTag(repo: String, tag:String) = Action {
    asJson {
      NotImplemented
      // TODO: save tag
    }
  }
}

