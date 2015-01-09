package controllers

import play.api.libs.iteratee.Enumerator
import play.api.libs.json._
import play.api.mvc._

import scala.language.reflectiveCalls
import scala.util.{Failure, Success, Try}

object RegistryController extends Controller {


  def _ping() = Action {
    Ok("Docker Registry").withHeaders("X-Docker-Registry-Version" -> "0.6.0")
  }

  def images(repo: String) = Action {
    Ok(Json.toJson("a"))
  }

  def putImages(repo: String) = Action {
    // TODO
    Status(204)("")
  }

  def putRepo(repo: String) = Action {
    Ok(Json.toJson("PUTPUT")).withHeaders("X-Docker-Token" -> "mytok").withHeaders("X-Docker-Endpoints" -> "localhost:9000")
  }

  def getImageJson(image: String) = Action {
    NotFound("bla")
  }

  def putImageJson(image: String) = Action {
    // TODO: write JSON file
    Ok(Json.toJson("OK"))
  }

  def putImageLayer(image: String) = Action {
    // TODO: write binary layer file
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

