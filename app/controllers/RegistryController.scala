package controllers

import play.api.libs.json._
import play.api.mvc._

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

object RegistryController extends Controller {

  implicit val fileCodec = scala.io.Codec.UTF8

  val dataPath = Paths.get(app.configuration.getString("registry.data.path").getOrElse("data"))
  val imagesPath = dataPath.resolve("images")
  val repoPath = dataPath.resolve("repositories")
  val JSON_SUFFIX = ".json"

  private def fileNameWithoutSuffix(filename: String, suffix: String = JSON_SUFFIX) =
    filename.substring(0, filename.length - suffix.length)

  def root() = Action {
    Ok(Json.toJson("Docker Registry"))
  }

  def _ping() = Action {
    Ok(Json.toJson(true)).withHeaders(REGISTRY_VERSION -> "0.6.3")
  }

  def images(repo: String) = Action {
    Files.createDirectories(imagesPath)
    val files = imagesPath.toFile.list.filter(_.endsWith(JSON_SUFFIX))
    Ok(Json.toJson(files map { fn => Json.obj("id" -> fileNameWithoutSuffix(fn), "checksum" -> "foobar")}))
  }


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

  def putImages(repo: String) = Action {
    // this is the final call from Docker client when pushing an image
    // Docker client expects HTTP status code 204 (No Content) instead of 200 here!
    Status(204)("")
  }

  def putRepo(repo: String) = Action { request =>
    val host = request.headers.get("Host").getOrElse("")
    Ok(Json.toJson("PUTPUT"))
      .withHeaders(TOKEN -> "mytok")
      .withHeaders(ENDPOINTS -> host)
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

  def getAncestry(image: String, r: Seq[String] = Seq.empty): Option[List[String]] = {
    var ancestry = List(image)
    var cur = image
    while (true) {
      val imagePath = imagesPath.resolve(s"$cur.json")
      if (!Files.exists(imagePath)) {
        return None
      }
      val contents = Source.fromFile(imagePath.toFile).mkString
      val data = Json.parse(contents)
      (data \ "parent").asOpt[String] match {
        case Some(parent) =>
          cur = parent
          ancestry :+= cur
        case None =>
          return Some(ancestry)
      }
    }
    Some(ancestry)
  }

  /* not implemented yet */
  private case class Image(name: ImageName) {
    val path: Option[Path] = {
      val jsonPath = imagesPath.resolve(s"$name.json")
      Some(jsonPath).filter(Files.exists(_))
    }

    val parent: Option[Image] = {
      path
        .map { p =>
        val contents = Source.fromFile(p.toFile).mkString
        val data = Json.parse(contents)
        (data \ "parent").asOpt[ImageName].map(Image)
      }
        .flatten
    }
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
    val bodyData = request.body.moveTo(layerPath.toFile)
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
}

