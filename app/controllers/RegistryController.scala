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
}

