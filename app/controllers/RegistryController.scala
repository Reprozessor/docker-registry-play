package controllers

import com.wordnik.swagger.annotations.{ApiOperation, Api}
import play.api.libs.json._
import play.api.mvc._

import scala.annotation.tailrec
import scala.language.reflectiveCalls
import scala.io._

import java.nio.file.{Path, Paths, Files, StandardOpenOption}
import java.nio.charset.StandardCharsets

object DockerHeaders {
  val REGISTRY_VERSION = "X-Docker-Registry-Version"
  val REGISTRY_STANDALONE = "X-Docker-Registry-Standalone"
  val TOKEN = "X-Docker-Token"
  val ENDPOINTS = "X-Docker-Endpoints"
}

import DockerHeaders._

object CustomTypes {
  type ImageName = String
}

import CustomTypes._
import play.api.Play.{current => app}

@Api(value = "/registry")
object RegistryController extends Controller {

  implicit val fileCodec = scala.io.Codec.UTF8

  val dataPath = Paths.get(app.configuration.getString("registry.data.path").getOrElse("data"))
  val imagesPath = dataPath.resolve("images")
  val repoPath = dataPath.resolve("repositories")
  val JSON_SUFFIX = ".json"

  private def fileNameWithoutSuffix(filename: String, suffix: String = JSON_SUFFIX) =
    filename.substring(0, filename.length - suffix.length)

  @ApiOperation(value="Ping")
  def _ping() = Action {
    Ok(Json.toJson(true)).withHeaders(REGISTRY_VERSION -> "0.6.3")
  }

  @ApiOperation(value="Get Images")
  def getImages(repo: String) = Action {
    Files.createDirectories(imagesPath)
    val files = imagesPath.toFile.list.filter(_.endsWith(JSON_SUFFIX))
    Ok(Json.toJson(files map { fn => Json.obj("id" -> fileNameWithoutSuffix(fn), "checksum" -> "foobar")}))
  }

  @ApiOperation(value="Get Tags")
  def getTags(repo: String) = Action {
    val tagsPath: Path = repoPath.resolve(s"$repo/tags")
    if (Files.exists(tagsPath)) {
      val files: Array[String] = tagsPath.toFile.list().filter(_.endsWith(JSON_SUFFIX))
      Ok {
        Json.toJson(
          files.map { fn =>
            assert(implicitly[scala.io.Codec] == scala.io.Codec.UTF8)
            val contents = Source.fromFile(tagsPath.resolve(fn).toFile).mkString
            val name = fn.substring(0, fn.length - JSON_SUFFIX.length)
            name -> Json.parse(contents)
          }.toMap
        )
      }
    } else NotFound(Json.toJson(s"Repository $repo does not exist"))
  }

  @ApiOperation(value="Put Images")
  def putImages(repo: String) = Action {
    // this is the final call from Docker client when pushing an image
    // Docker client expects HTTP status code 204 (No Content) instead of 200 here!
    Status(204)("")
  }

  @ApiOperation(value="Put Repo")
  def putRepo(repo: String) = Action { request =>
    val hostHeader = request.headers.get("Host")
    val result = for {
      host <- hostHeader
    } yield Ok(Json.toJson("PUTPUT"))
      .withHeaders(TOKEN -> "mytok")
      .withHeaders(ENDPOINTS -> host)
    result.getOrElse(BadRequest("Host header missing"))
  }

  def getImageJson(image: ImageName) = Action {
    val imagePath = imagesPath.resolve(s"$image.json")
    val layerPath = imagesPath.resolve(s"$image.layer")
    if (Files.exists(imagePath) && Files.exists(layerPath)) {
      val contents = Source.fromFile(imagePath.toFile).mkString
      Ok(Json.parse(contents))
    } else {
      NotFound(Json.toJson(s"Image JSON ($image.json) not found"))
    }
  }

  @tailrec
  def getAncestry(image: String, ancestry: Seq[String] = Nil): Option[Seq[String]] = {
    val imagePath = imagesPath.resolve(s"$image.json")
    if (Files.exists(imagePath)) {
      val newAncestry = ancestry :+ image
      val contents = Source.fromFile(imagePath.toFile).mkString
      val data = Json.parse(contents)
      (data \ "parent").asOpt[String] match {
        case Some(parent) =>
          getAncestry(parent, newAncestry)
        case None =>
          Some(newAncestry)
      }
    } else None
  }

  def getImageAncestry(image: String) = Action {
    getAncestry(image) match {
      case Some(ancestry) => Ok(Json.toJson(ancestry))
      case None => NotFound(Json.toJson(s"Image JSON ($image.json) not found"))
    }
  }

  def putImageJson(image: String) = Action(BodyParsers.parse.json) { request =>
    Files.createDirectories(imagesPath)
    val contents: String = Json.stringify(request.body)

    Files.write(imagesPath.resolve(s"$image.json"), contents.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE)
    Ok(Json.toJson("OK"))
  }

  def putImageLayer(image: String) = Action(BodyParsers.parse.temporaryFile) { request =>
    val layerPath = imagesPath.resolve(s"$image.layer")
    request.body.moveTo(layerPath.toFile)
    Ok(Json.toJson("OK"))
  }

  def getImageLayer(image: String) = Action {
    val layerPath = imagesPath.resolve(s"$image.layer")
    if (Files.exists(layerPath)) {
      Ok.sendFile(layerPath.toFile)
    } else {
      NotFound("Layer not found")
    }
  }

  def putImageChecksum(image: String) = Action {
    // TODO: do something
    Ok(Json.toJson("OK"))
  }

  def putTag(repo: String, tag: String) = Action(BodyParsers.parse.json) { request =>
    val tagPath = repoPath.resolve(s"$repo/tags/$tag.json")
    Files.createDirectories(tagPath.getParent)
    val contents: String = Json.stringify(request.body)
    Files.write(tagPath, contents.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE)
    Ok(Json.toJson("OK"))
  }

  @ApiOperation("Search")
  def search(q: String) = Action {
    NotImplemented
  }
}

