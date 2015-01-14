import org.specs2.mutable._
import org.specs2.execute.{AsResult, Result}
import play.api.libs.json._
import play.api.test.Helpers._
import play.api.test.{FakeRequest, WithApplication, FakeApplication}
import controllers.DockerHeaders._
import scala.util.Random
import java.io.File

object Utils {
  val random = new java.util.Random
  def randomString(prefix: String) = {
    prefix + java.lang.Integer.toHexString(random.nextInt)
  }
  def delete(files: Iterable[File]): Unit = files.foreach(delete)
  def delete(file: File)
  {
      if(file.isDirectory)
      {
        delete(listFiles(file))
        file.delete
      }
      else if(file.exists)
        file.delete
  }
  def listFiles(filter: java.io.FileFilter)(dir: File): Array[File] = wrapNull(dir.listFiles(filter))
  def listFiles(dir: File, filter: java.io.FileFilter): Array[File] = wrapNull(dir.listFiles(filter))
  def listFiles(dir: File): Array[File] = wrapNull(dir.listFiles())
  private def wrapNull(a: Array[File]) =
  {
    if(a == null)
      new Array[File](0)
    else
      a
  }
}

abstract class WithTestApplication(
  override val app: FakeApplication = FakeApplication(additionalConfiguration = Map("registry.data.path" -> Utils.randomString("target/test-data-") ))
)
extends WithApplication {
  override def around[T: AsResult](t: => T): Result = super.around {
    val result = t
    cleanupData
    result
  }

  def cleanupData = {
    val path = new File(app.configuration.getString("registry.data.path").get)
    Utils.delete(path)
  }

}


class RegistryControllerTest extends Specification {
  "RegistryController" should {
    "return server name" in new WithTestApplication {
      val res = route(FakeRequest(GET, "/")).get

      status(res) must_== 200
    }
    "respond to _ping" in new WithTestApplication {
      val res = route(FakeRequest(GET, "/v1/_ping")).get

      val reportedDockerRegistryVersion = header(REGISTRY_VERSION, res).getOrElse("UNKNOWN")

      reportedDockerRegistryVersion must_== "0.6.3"

      status(res) must_== 200
      contentAsJson(res) match {
        case JsBoolean(value) => value must_== true
        case other => sys.error(s"Unexpected result type: $other")
      }
    }
    "fail on getting JSON image for non-existing image" in new WithTestApplication {
      val res = route(FakeRequest(GET, "/v1/images/NONEXISTING/json")).get
      status(res) must_== 404
    }
    "get list of images" in new WithTestApplication {
      val res = route(FakeRequest(GET, "/v1/repositories/some/repo/images")).get
      status(res) must_== 200
    }
    "fail on getting ancestry for non-existing image" in new WithTestApplication {
      val res = route(FakeRequest(GET, "/v1/images/NONEXISTING/ancestry")).get
      status(res) must_== 404
    }
    "fail on getting layer for non-existing image" in new WithTestApplication {
      val res = route(FakeRequest(GET, "/v1/images/NONEXISTING/layer")).get
      status(res) must_== 404
    }
    "push and verify layer" in new WithTestApplication {
      var res = route(FakeRequest(PUT, "/v1/images/abc123/layer").withTextBody("some data")).get
      status(res) must_== 200

      res = route(FakeRequest(GET, "/v1/images/abc123/layer")).get
      contentAsString(res) must_== "some data"
    }
    "push and verify tag" in new WithTestApplication {
      var res = route(FakeRequest(PUT, "/v1/repositories/test/repo1/tags/1.0").withJsonBody(JsString("abc123-image-id"))).get
      status(res) must_== 200
      contentAsJson(res) must_== JsString("OK")
    }
    "simulate Docker client push behavior" in new WithTestApplication {
      var res = route(FakeRequest(PUT, "/v1/repositories/test/repo/").withJsonBody(Json.parse("""[{"id":"511136ea3c5a64f264b78b5433614aec563103b4d4702f3ba7d4d2698e22c158"},{"id":"df7546f9f060a2268024c8a230d8639878585defcc1bc6f79d2728a13957871b"},{"id":"ea13149945cb6b1e746bf28032f02e9b5a793523481a0a18645fc77ad53c4ea2"},{"id":"4986bf8c15363d1c5d15512d5266f8777bfba4974ac56e3270e7760f6f0a8125","Tag":"1.0"}]"""))).get
      status(res) must_== 200

      res = route(FakeRequest(GET, "/v1/images/511136ea3c5a64f264b78b5433614aec563103b4d4702f3ba7d4d2698e22c158/json")).get
      status(res) must_== 404

      res = route(FakeRequest(PUT, "/v1/images/511136ea3c5a64f264b78b5433614aec563103b4d4702f3ba7d4d2698e22c158/json").withJsonBody(Json.parse("""{"id":"511136ea3c5a64f264b78b5433614aec563103b4d4702f3ba7d4d2698e22c158","comment":"Imported from -","created":"2013-06-13T14:03:50.821769-07:00","container_config":{"Hostname":"","User":"","Memory":0,"MemorySwap":0,"CpuShares":0,"AttachStdin":false,"AttachStdout":false,"AttachStderr":false,"PortSpecs":null,"Tty":false,"OpenStdin":false,"StdinOnce":false,"Env":null,"Cmd":null,"Dns":null,"Image":"","Volumes":null,"VolumesFrom":""},"docker_version":"0.4.0","architecture":"x86_64"}"""))).get
      contentAsJson(res) must_== JsString("OK")

      res = route(FakeRequest(PUT, "/v1/images/511136ea3c5a64f264b78b5433614aec563103b4d4702f3ba7d4d2698e22c158/layer").withTextBody("some data")).get
      status(res) must_== 200

      res = route(FakeRequest(PUT, "/v1/images/511136ea3c5a64f264b78b5433614aec563103b4d4702f3ba7d4d2698e22c158/checksum")).get
      status(res) must_== 200
      contentAsJson(res) must_== JsString("OK")

    }
  }

}
