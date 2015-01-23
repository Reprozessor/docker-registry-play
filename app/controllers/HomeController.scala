package controllers

import com.wordnik.swagger.annotations.{ApiOperation, Api}
import play.api.libs.json._
import play.api.mvc._

import scala.annotation.tailrec
import scala.language.reflectiveCalls
import scala.io._

import java.nio.file.{Path, Paths, Files, StandardOpenOption}
import java.nio.charset.StandardCharsets

object HomeController extends Controller {

  def index() = Action { request =>
    Ok(views.html.index(s"http://${request.host}/swagger.json"))
  }

  def getOptions(wholepath: String) = Action {
    Ok(JsString("OK"))
  }

}

