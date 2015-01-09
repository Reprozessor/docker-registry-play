package controllers

import play.api.libs.iteratee.Enumerator
import play.api.libs.json._
import play.api.mvc._

import scala.language.reflectiveCalls
import scala.util.{Failure, Success, Try}

object RootController extends Controller {

  def index() = Action {
    Ok("Index")
  }

}


