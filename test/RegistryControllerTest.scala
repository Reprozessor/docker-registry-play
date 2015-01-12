import org.specs2.mutable._
import play.api.libs.json.JsBoolean
import play.api.test.Helpers._
import play.api.test.{FakeRequest, WithApplication}
import controllers.DockerHeaders._

class RegistryControllerTest extends Specification {
  "RegistryController" should {
    "return server name" in new WithApplication {
      val res = route(FakeRequest(GET, "/")).get

      status(res) must_== 200
    }
    "respond to _ping" in new WithApplication {
      val res = route(FakeRequest(GET, "/v1/_ping")).get

      val reportedDockerRegistryVersion = header(REGISTRY_VERSION, res).getOrElse("UNKNOWN")

      reportedDockerRegistryVersion must_== "0.6.3"

      status(res) must_== 200
      contentAsJson(res) match {
        case JsBoolean(value) => value must_== true
        case other => sys.error(s"Unexpected result type: $other")
      }
    }
    "fail on getting JSON image for non-existing image" in new WithApplication {
      val res = route(FakeRequest(GET, "/v1/images/NONEXISTING/json")).get
      status(res) must_== 404
    }
    "get list of images" in new WithApplication {
      val res = route(FakeRequest(GET, "/v1/repositories/some/repo/images")).get
      status(res) must_== 200
    }
    "fail on getting ancestry for non-existing image" in new WithApplication {
      val res = route(FakeRequest(GET, "/v1/images/NONEXISTING/ancestry")).get
      status(res) must_== 404
    }
    "fail on getting layer for non-existing image" in new WithApplication {
      val res = route(FakeRequest(GET, "/v1/images/NONEXISTING/layer")).get
      status(res) must_== 404
    }
  }

}
